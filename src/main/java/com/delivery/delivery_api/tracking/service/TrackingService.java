package com.delivery.delivery_api.tracking.service;

import com.delivery.delivery_api.driver.entity.Driver;
import com.delivery.delivery_api.driver.service.IDriverService;
import com.delivery.delivery_api.order.entity.Order;
import com.delivery.delivery_api.order.repository.OrderRepository;
import com.delivery.delivery_api.shared.exception.BusinessException;
import com.delivery.delivery_api.shared.exception.ResourceNotFoundException;
import com.delivery.delivery_api.tracking.dto.request.LocationUpdateRequest;
import com.delivery.delivery_api.tracking.dto.response.LocationResponse;
import com.delivery.delivery_api.tracking.entity.DriverLocation;
import com.delivery.delivery_api.tracking.repository.DriverLocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrackingService implements ITrackingService {

    private final DriverLocationRepository locationRepository;
    private final OrderRepository orderRepository;
    private final IDriverService driverService;
    private final SimpMessagingTemplate messagingTemplate;


    @Override
    @Transactional
    public void updateLocation(LocationUpdateRequest request) {
        Driver driver = driverService.getAuthenticatedDriver();

        Order order = null;
        if (request.getOrderUuid() != null) {
            order = orderRepository.findByUuid(request.getOrderUuid())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Commande", "uuid", request.getOrderUuid()));

            if (order.getDriver() == null ||
                    !order.getDriver().getId().equals(driver.getId())) {
                throw new BusinessException(
                        "Cette commande ne vous est pas assignée",
                        "ORDER_NOT_ASSIGNED",
                        HttpStatus.FORBIDDEN
                );
            }
        }

        DriverLocation location = DriverLocation.builder()
                .driver(driver)
                .order(order)
                .lat(request.getLat())
                .lng(request.getLng())
                .build();
        locationRepository.save(location);

        LocationResponse locationResponse = buildResponse(location, driver);
        publishLocation(driver, order, locationResponse);

        log.debug("Position mise à jour — livreur: {} lat:{} lng:{}",
                driver.getUser().getEmail(), request.getLat(), request.getLng());
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponse getLatestDriverLocation(String driverUuid) {
        Driver driver = driverService.getByUuidOrThrow(driverUuid);

        List<DriverLocation> locations = locationRepository.findLatestByDriverId(
                driver.getId(), PageRequest.of(0, 1));

        if (locations.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Position", "driverUuid", driverUuid);
        }

        return buildResponse(locations.get(0), driver);
    }

    @Override
    @Transactional(readOnly = true)
    public LocationResponse getOrderDriverLocation(String orderUuid) {
        Order order = orderRepository.findByUuid(orderUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Commande", "uuid", orderUuid));

        if (order.getDriver() == null) {
            throw new BusinessException(
                    "Aucun livreur n'est encore assigné à cette commande",
                    "NO_DRIVER_ASSIGNED",
                    HttpStatus.BAD_REQUEST
            );
        }

        List<DriverLocation> locations = locationRepository.findLatestByDriverAndOrder(
                order.getDriver().getId(), order.getId(), PageRequest.of(0, 1));

        if (locations.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Position du livreur introuvable pour cette commande", "orderUuid", orderUuid);
        }

        return buildResponse(locations.get(0), order.getDriver());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LocationResponse> getOrderLocationHistory(String orderUuid) {
        Order order = orderRepository.findByUuid(orderUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Commande", "uuid", orderUuid));

        return locationRepository.findByOrderIdOrderByCreatedAtAsc(order.getId())
                .stream()
                .map(l -> buildResponse(l, l.getDriver()))
                .toList();
    }

    // ===== WEBSOCKET =====

    /**
     * Publie la position du livreur sur deux canaux WebSocket :
     *
     * 1. /topic/tracking.driver.{driverUuid}
     *    → Manager/gestionnaire qui surveille tous ses livreurs sur une carte
     *
     * 2. /topic/tracking.order.{orderUuid}
     *    → Client qui suit sa commande spécifique en temps réel
     */
    private void publishLocation(Driver driver, Order order, LocationResponse response) {
        messagingTemplate.convertAndSend(
                "/topic/tracking.driver." + driver.getUuid(),
                response
        );

        if (order != null) {
            messagingTemplate.convertAndSend(
                    "/topic/tracking.order." + order.getUuid(),
                    response
            );
        }

        log.debug("Position publiée sur WebSocket — driver: {}", driver.getUuid());
    }

    private LocationResponse buildResponse(DriverLocation location, Driver driver) {
        return LocationResponse.builder()
                .driverUuid(driver.getUuid())
                .driverName(driver.getFirstName() + " " + driver.getLastName())
                .lat(location.getLat())
                .lng(location.getLng())
                .orderUuid(location.getOrder() != null
                        ? location.getOrder().getUuid() : null)
                .timestamp(location.getCreatedAt())
                .build();
    }
}