package com.delivery.delivery_api.order.service;

import com.delivery.delivery_api.agency.entity.Agency;
import com.delivery.delivery_api.agency.service.AgencyService;
import com.delivery.delivery_api.customer.entity.Customer;
import com.delivery.delivery_api.customer.service.ICustomerService;
import com.delivery.delivery_api.device.service.IDeviceService;
import com.delivery.delivery_api.driver.entity.Driver;
import com.delivery.delivery_api.driver.enums.DriverStatus;
import com.delivery.delivery_api.driver.repository.DriverRepository;
import com.delivery.delivery_api.driver.service.IDriverService;
import com.delivery.delivery_api.fee.service.IFeeService;
import com.delivery.delivery_api.order.dto.request.AssignDriverRequest;
import com.delivery.delivery_api.order.dto.request.CreateOrderRequest;
import com.delivery.delivery_api.order.dto.request.UpdateOrderStatusRequest;
import com.delivery.delivery_api.order.dto.response.OrderResponse;
import com.delivery.delivery_api.order.dto.response.OrderStatusHistoryResponse;
import com.delivery.delivery_api.order.entity.Order;
import com.delivery.delivery_api.order.entity.OrderStatusHistory;
import com.delivery.delivery_api.order.enums.OrderStatus;
import com.delivery.delivery_api.order.repository.OrderRepository;
import com.delivery.delivery_api.order.repository.OrderStatusHistoryRepository;
import com.delivery.delivery_api.shared.audit.AuditClient;
import com.delivery.delivery_api.shared.exception.BusinessException;
import com.delivery.delivery_api.shared.exception.ResourceNotFoundException;
import com.delivery.delivery_api.shared.notification.NotificationClient;
import com.delivery.delivery_api.shared.response.PageResponse;
import com.delivery.delivery_api.shared.utils.PaginationUtil;
import com.delivery.delivery_api.shared.utils.UuidGenerator;
import com.delivery.delivery_api.zone.entity.Zone;
import com.delivery.delivery_api.zone.service.IZoneService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService {

    private final OrderRepository orderRepository;
    private final OrderStatusHistoryRepository statusHistoryRepository;
    private final ICustomerService customerService;
    private final IDriverService driverService;
    private final DriverRepository driverRepository;
    private final AgencyService agencyService;
    private final IZoneService zoneService;
    private final IFeeService feeService;
    private final IDeviceService deviceService;
    private final NotificationClient notificationClient;
    private final AuditClient auditClient;


    @Override
    @Transactional
    public OrderResponse create(CreateOrderRequest request) {
        Customer customer = customerService.getAuthenticatedCustomer();

        if (!customer.isProfileComplete()) {
            throw new BusinessException(
                    "Votre profil doit être complet pour passer une commande",
                    "PROFILE_INCOMPLETE",
                    HttpStatus.BAD_REQUEST
            );
        }

        agencyService.validateAgencyActive(request.getAgencyId());
        Agency agency = agencyService.getByIdOrThrow(request.getAgencyId());

        Zone zone = null;
        if (request.getZoneId() != null) {
            zoneService.validateZoneActive(request.getZoneId());
            zone = zoneService.getByIdOrThrow(request.getZoneId());
        }

        Double estimatedPrice = zone != null ? zone.getBaseFee() : 0.0;

        Double commissionAmount = 0.0;
        try {
            commissionAmount = feeService.calculateCommission(
                    request.getAgencyId(), estimatedPrice);
        } catch (Exception e) {
            log.warn("Impossible de calculer la commission pour agence {} : {}",
                    request.getAgencyId(), e.getMessage());
        }

        Order order = Order.builder()
                .uuid(UuidGenerator.generate())
                .orderCode(UuidGenerator.generateOrderCode())
                .customer(customer)
                .agency(agency)
                .zone(zone)
                .departureAddress(request.getDepartureAddress())
                .departureLat(request.getDepartureLat())
                .departureLng(request.getDepartureLng())
                .arrivalAddress(request.getArrivalAddress())
                .arrivalLat(request.getArrivalLat())
                .arrivalLng(request.getArrivalLng())
                .estimatedPrice(estimatedPrice)
                .commissionAmount(commissionAmount)
                .status(OrderStatus.PENDING)
                .note(request.getNote())
                .build();

        order = orderRepository.save(order);

        saveStatusHistory(order, OrderStatus.PENDING, getConnectedUserEmail(), "Commande créée");

        sendPushToUser(
                customer.getUser().getId(),
                "Commande créée ✅",
                "Votre commande " + order.getOrderCode() + " est en attente d'un livreur",
                Map.of("orderUuid", order.getUuid(), "type", "ORDER_CREATED")
        );

        auditClient.log(
                "ORDER_CREATED",
                customer.getUser().getEmail(),
                "Commande créée : " + order.getOrderCode(),
                null
        );

        log.info("Commande créée : {} par {}", order.getOrderCode(),
                customer.getUser().getEmail());

        return toResponse(order, false);
    }

    @Override
    @Transactional
    public OrderResponse assignDriver(String orderUuid, AssignDriverRequest request) {
        Order order = getByUuidOrThrow(orderUuid);

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(
                    "Seules les commandes en attente peuvent être assignées",
                    "INVALID_ORDER_STATUS",
                    HttpStatus.BAD_REQUEST
            );
        }

        Driver driver = driverService.getByUuidOrThrow(request.getDriverUuid());

        if (!driver.getAgency().getId().equals(order.getAgency().getId())) {
            throw new BusinessException(
                    "Ce livreur n'appartient pas à l'agence de cette commande",
                    "DRIVER_AGENCY_MISMATCH",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (!driver.isAvailable()) {
            throw new BusinessException(
                    "Ce livreur n'est pas disponible",
                    "DRIVER_NOT_AVAILABLE",
                    HttpStatus.BAD_REQUEST
            );
        }

        orderRepository.assignDriver(orderUuid, driver.getId());
        driverRepository.updateStatus(driver.getUuid(), DriverStatus.BUSY);

        order = getByUuidOrThrow(orderUuid);

        saveStatusHistory(order, OrderStatus.ACCEPTED,
                getConnectedUserEmail(), "Livreur assigné : " + driver.getFirstName());

        sendPushToUser(
                order.getCustomer().getUser().getId(),
                "Livreur assigné !",
                driver.getFirstName() + " prend en charge votre commande",
                Map.of("orderUuid", order.getUuid(), "type", "DRIVER_ASSIGNED")
        );

        sendPushToUser(
                driver.getUser().getId(),
                "Nouvelle commande !",
                "Commande " + order.getOrderCode() + " vous a été assignée",
                Map.of("orderUuid", order.getUuid(), "type", "ORDER_ASSIGNED")
        );

        auditClient.log(
                "ORDER_ASSIGNED",
                getConnectedUserEmail(),
                String.format("Commande %s assignée au livreur %s %s",
                        order.getOrderCode(), driver.getFirstName(), driver.getLastName()),
                null
        );

        log.info("Commande {} assignée au livreur {}",
                order.getOrderCode(), driver.getUser().getEmail());

        return toResponse(order, true);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(String orderUuid, UpdateOrderStatusRequest request) {
        Order order = getByUuidOrThrow(orderUuid);
        OrderStatus newStatus = request.getStatus();

        validateStatusTransition(order.getStatus(), newStatus);

        orderRepository.updateStatus(orderUuid, newStatus);

        if (newStatus == OrderStatus.DELIVERED) {
            order.setDeliveredAt(LocalDateTime.now());
            order.setFinalPrice(order.getEstimatedPrice());
            orderRepository.save(order);

            if (order.getDriver() != null) {
                driverRepository.updateStatus(
                        order.getDriver().getUuid(), DriverStatus.AVAILABLE);
            }

            sendPushToUser(
                    order.getCustomer().getUser().getId(),
                    "Colis livré !!!",
                    "Votre commande " + order.getOrderCode() + " a été livrée avec succès",
                    Map.of("orderUuid", order.getUuid(), "type", "ORDER_DELIVERED")
            );
        }

        if (newStatus == OrderStatus.CANCELLED && order.getDriver() != null) {
            driverRepository.updateStatus(
                    order.getDriver().getUuid(), DriverStatus.AVAILABLE);

            order.setCancellationReason(request.getCancellationReason());
            orderRepository.save(order);

            sendPushToUser(
                    order.getCustomer().getUser().getId(),
                    "Commande annulée !!!",
                    "Votre commande " + order.getOrderCode() + " a été annulée",
                    Map.of("orderUuid", order.getUuid(), "type", "ORDER_CANCELLED")
            );
        }

        if (newStatus == OrderStatus.IN_TRANSIT) {
            sendPushToUser(
                    order.getCustomer().getUser().getId(),
                    "Colis en route !!!",
                    "Votre livreur est en route vers vous",
                    Map.of("orderUuid", order.getUuid(), "type", "ORDER_IN_TRANSIT")
            );
        }

        order = getByUuidOrThrow(orderUuid);
        saveStatusHistory(order, newStatus, getConnectedUserEmail(), request.getNote());

        auditClient.log(
                "ORDER_STATUS_UPDATED",
                getConnectedUserEmail(),
                String.format("Commande %s → %s", order.getOrderCode(), newStatus),
                null
        );

        log.info("Statut commande {} → {}", order.getOrderCode(), newStatus);

        return toResponse(order, true);
    }

    @Override
    @Transactional
    public OrderResponse cancel(String orderUuid, String reason) {
        Order order = getByUuidOrThrow(orderUuid);

        if (!order.isCancellable()) {
            throw new BusinessException(
                    "Cette commande ne peut plus être annulée",
                    "ORDER_NOT_CANCELLABLE",
                    HttpStatus.BAD_REQUEST
            );
        }

        orderRepository.updateStatus(orderUuid, OrderStatus.CANCELLED);
        order.setCancellationReason(reason);
        orderRepository.save(order);

        if (order.getDriver() != null) {
            driverRepository.updateStatus(order.getDriver().getUuid(), DriverStatus.AVAILABLE);
        }

        order = getByUuidOrThrow(orderUuid);
        saveStatusHistory(order, OrderStatus.CANCELLED, getConnectedUserEmail(), reason);

        sendPushToUser(
                order.getCustomer().getUser().getId(),
                "Commande annulée",
                "Votre commande " + order.getOrderCode() + " a été annulée",
                Map.of("orderUuid", order.getUuid(), "type", "ORDER_CANCELLED")
        );

        auditClient.log("ORDER_CANCELLED", getConnectedUserEmail(),
                "Commande annulée : " + order.getOrderCode() + " — " + reason, null);

        return toResponse(order, true);
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findByUuid(String uuid) {
        Order order = getByUuidOrThrow(uuid);
        return toResponse(order, true);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getMyOrders(int page, int size) {
        Customer customer = customerService.getAuthenticatedCustomer();
        Page<Order> orders = orderRepository.findByCustomerId(
                customer.getId(), PaginationUtil.build(page, size));
        List<OrderResponse> content = orders.getContent().stream()
                .map(o -> toResponse(o, false))
                .toList();
        return PageResponse.from(orders, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getCustomerOrders(String customerUuid,
                                                         int page, int size) {
        Customer customer = customerService.getByUuidOrThrow(customerUuid);
        Page<Order> orders = orderRepository.findByCustomerId(
                customer.getId(), PaginationUtil.build(page, size));
        List<OrderResponse> content = orders.getContent().stream()
                .map(o -> toResponse(o, false))
                .toList();
        return PageResponse.from(orders, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getMyDriverOrders(int page, int size) {
        Driver driver = driverService.getAuthenticatedDriver();
        Page<Order> orders = orderRepository.findByDriverId(
                driver.getId(), PaginationUtil.build(page, size));
        List<OrderResponse> content = orders.getContent().stream()
                .map(o -> toResponse(o, false))
                .toList();
        return PageResponse.from(orders, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getAgencyOrders(Long agencyId, int page, int size) {
        agencyService.validateAgencyExists(agencyId);
        Page<Order> orders = orderRepository.findByAgencyId(
                agencyId, PaginationUtil.build(page, size));
        List<OrderResponse> content = orders.getContent().stream()
                .map(o -> toResponse(o, false))
                .toList();
        return PageResponse.from(orders, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderResponse> getPendingOrdersByAgency(Long agencyId,
                                                                int page, int size) {
        agencyService.validateAgencyExists(agencyId);
        Page<Order> orders = orderRepository.findPendingByAgency(
                agencyId, PaginationUtil.build(page, size));
        List<OrderResponse> content = orders.getContent().stream()
                .map(o -> toResponse(o, false))
                .toList();
        return PageResponse.from(orders, content);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderStatusHistoryResponse> getStatusHistory(String orderUuid) {
        return statusHistoryRepository.findByOrderUuidOrderByCreatedAtAsc(orderUuid)
                .stream()
                .map(h -> OrderStatusHistoryResponse.builder()
                        .status(h.getStatus())
                        .changedBy(h.getChangedBy())
                        .note(h.getNote())
                        .createdAt(h.getCreatedAt())
                        .build())
                .toList();
    }

    private Order getByUuidOrThrow(String uuid) {
        return orderRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Commande", "uuid", uuid));
    }

    private void saveStatusHistory(Order order, OrderStatus status,
                                   String changedBy, String note) {
        OrderStatusHistory history = OrderStatusHistory.builder()
                .order(order)
                .status(status)
                .changedBy(changedBy)
                .note(note)
                .build();
        statusHistoryRepository.save(history);
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus next) {
        boolean valid = switch (current) {
            case PENDING    -> next == OrderStatus.ACCEPTED || next == OrderStatus.CANCELLED;
            case ACCEPTED   -> next == OrderStatus.PICKUP   || next == OrderStatus.CANCELLED;
            case PICKUP     -> next == OrderStatus.IN_TRANSIT;
            case IN_TRANSIT -> next == OrderStatus.DELIVERED;
            case DELIVERED, CANCELLED -> false;
        };

        if (!valid) {
            throw new BusinessException(
                    String.format("Transition de statut invalide : %s → %s", current, next),
                    "INVALID_STATUS_TRANSITION",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private void sendPushToUser(Long userId, String title, String body,
                                Map<String, String> data) {
        try {
            List<String> tokens = deviceService.getActiveTokensByUserId(userId);
            for (String token : tokens) {
                notificationClient.sendPush(token, title, body, data);
            }
        } catch (Exception e) {
            log.warn("Impossible d'envoyer la notification push : {}", e.getMessage());
        }
    }

    private String getConnectedUserEmail() {
        try {
            return SecurityContextHolder.getContext()
                    .getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }

    private OrderResponse toResponse(Order order, boolean withHistory) {
        OrderResponse.OrderResponseBuilder builder = OrderResponse.builder()
                .uuid(order.getUuid())
                .orderCode(order.getOrderCode())
                .customerUuid(order.getCustomer().getUuid())
                .customerName(order.getCustomer().getFirstName() + " "
                        + order.getCustomer().getLastName())
                .agencyUuid(order.getAgency().getUuid())
                .agencyName(order.getAgency().getName())
                .departureAddress(order.getDepartureAddress())
                .departureLat(order.getDepartureLat())
                .departureLng(order.getDepartureLng())
                .arrivalAddress(order.getArrivalAddress())
                .arrivalLat(order.getArrivalLat())
                .arrivalLng(order.getArrivalLng())
                .estimatedPrice(order.getEstimatedPrice())
                .finalPrice(order.getFinalPrice())
                .commissionAmount(order.getCommissionAmount())
                .status(order.getStatus())
                .note(order.getNote())
                .cancellationReason(order.getCancellationReason())
                .deliveredAt(order.getDeliveredAt())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt());

        if (order.getDriver() != null) {
            builder.driverUuid(order.getDriver().getUuid())
                    .driverName(order.getDriver().getFirstName() + " "
                            + order.getDriver().getLastName())
                    .driverTelephone(order.getDriver().getTelephone());
        }

        if (order.getZone() != null) {
            builder.zoneUuid(order.getZone().getUuid())
                    .zoneName(order.getZone().getName());
        }

        if (withHistory) {
            builder.statusHistory(getStatusHistory(order.getUuid()));
        }

        return builder.build();
    }
}