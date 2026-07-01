package com.delivery.delivery_api.payment.service;

import com.delivery.delivery_api.device.service.IDeviceService;
import com.delivery.delivery_api.fee.service.IFeeService;
import com.delivery.delivery_api.order.entity.Order;
import com.delivery.delivery_api.order.enums.OrderStatus;
import com.delivery.delivery_api.order.repository.OrderRepository;
import com.delivery.delivery_api.payment.dto.request.CreatePaymentRequest;
import com.delivery.delivery_api.payment.dto.response.PaymentResponse;
import com.delivery.delivery_api.payment.entity.Payment;
import com.delivery.delivery_api.payment.enums.PaymentMethod;
import com.delivery.delivery_api.payment.enums.PaymentStatus;
import com.delivery.delivery_api.payment.repository.PaymentRepository;
import com.delivery.delivery_api.shared.audit.AuditClient;
import com.delivery.delivery_api.shared.exception.BusinessException;
import com.delivery.delivery_api.shared.exception.ConflictException;
import com.delivery.delivery_api.shared.exception.ResourceNotFoundException;
import com.delivery.delivery_api.shared.notification.NotificationClient;
import com.delivery.delivery_api.shared.response.PageResponse;
import com.delivery.delivery_api.shared.utils.KeyGeneratorUtil;
import com.delivery.delivery_api.shared.utils.PaginationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService implements IPaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final IFeeService feeService;
    private final IDeviceService deviceService;
    private final NotificationClient notificationClient;
    private final AuditClient auditClient;

    @Override
    @Transactional
    public PaymentResponse create(CreatePaymentRequest request) {
        Order order = getOrderOrThrow(request.getOrderUuid());

        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException(
                    "Le paiement ne peut être initié que pour une commande livrée",
                    "ORDER_NOT_DELIVERED",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (paymentRepository.existsByOrderId(order.getId())) {
            throw new ConflictException(
                    "Un paiement existe déjà pour cette commande");
        }

        Double amount = order.getFinalPrice() != null
                ? order.getFinalPrice()
                : order.getEstimatedPrice();

        Double commissionAmount = 0.0;
        try {
            commissionAmount = feeService.calculateCommission(
                    order.getAgency().getId(), amount);
        } catch (Exception e) {
            log.warn("Impossible de calculer la commission : {}", e.getMessage());
        }

        Double agencyAmount = amount - commissionAmount;

        Payment payment = Payment.builder()
                .uuid(KeyGeneratorUtil.generateRandomToken(16))
                .order(order)
                .amount(amount)
                .commissionAmount(commissionAmount)
                .agencyAmount(agencyAmount)
                .method(request.getMethod())
                .status(PaymentStatus.PENDING)
                .transactionReference(request.getTransactionReference())
                .build();

        payment = paymentRepository.save(payment);

        // Pour CASH → confirmer directement (pas de validation externe)
        if (request.getMethod() == PaymentMethod.CASH) {
            payment = confirmPaymentInternal(payment, "CASH-" + payment.getUuid());
        }

        sendPushToUser(
                order.getCustomer().getUser().getId(),
                "Paiement initié 💳",
                String.format("Paiement de %.0f FCFA en cours pour la commande %s",
                        amount, order.getOrderCode()),
                Map.of("paymentUuid", payment.getUuid(), "type", "PAYMENT_INITIATED")
        );

        auditClient.log(
                "PAYMENT_CREATED",
                getConnectedUserEmail(),
                String.format("Paiement initié — commande: %s, montant: %.0f FCFA, méthode: %s",
                        order.getOrderCode(), amount, request.getMethod()),
                null
        );

        log.info("Paiement créé pour commande {} — montant: {} FCFA",
                order.getOrderCode(), amount);

        return toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse confirm(String paymentUuid, String transactionReference) {
        Payment payment = getByUuidOrThrow(paymentUuid);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(
                    "Ce paiement ne peut pas être confirmé — statut : " + payment.getStatus(),
                    "INVALID_PAYMENT_STATUS",
                    HttpStatus.BAD_REQUEST
            );
        }

        payment = confirmPaymentInternal(payment, transactionReference);

        sendPushToUser(
                payment.getOrder().getCustomer().getUser().getId(),
                "Paiement confirmé !!!",
                String.format("Votre paiement de %.0f FCFA a été validé",
                        payment.getAmount()),
                Map.of("paymentUuid", payment.getUuid(), "type", "PAYMENT_CONFIRMED")
        );

        notificationClient.sendEmail(
                payment.getOrder().getAgency().getEmail(),
                "Nouveau paiement reçu — " + payment.getOrder().getOrderCode(),
                "payment-confirmed",
                Map.of(
                        "orderCode", payment.getOrder().getOrderCode(),
                        "amount", payment.getAmount(),
                        "commission", payment.getCommissionAmount(),
                        "agencyAmount", payment.getAgencyAmount(),
                        "method", payment.getMethod().name()
                )
        );

        auditClient.log(
                "PAYMENT_CONFIRMED",
                getConnectedUserEmail(),
                String.format("Paiement confirmé — commande: %s, ref: %s",
                        payment.getOrder().getOrderCode(), transactionReference),
                null
        );

        log.info("Paiement confirmé : {} — ref: {}", paymentUuid, transactionReference);

        return toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse markAsFailed(String paymentUuid, String reason) {
        Payment payment = getByUuidOrThrow(paymentUuid);

        if (payment.getStatus() != PaymentStatus.PENDING) {
            throw new BusinessException(
                    "Ce paiement ne peut pas être marqué comme échoué",
                    "INVALID_PAYMENT_STATUS",
                    HttpStatus.BAD_REQUEST
            );
        }

        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(reason);
        payment = paymentRepository.save(payment);

        sendPushToUser(
                payment.getOrder().getCustomer().getUser().getId(),
                "Paiement échoué ❌",
                "Votre paiement a échoué. Veuillez réessayer.",
                Map.of("paymentUuid", payment.getUuid(), "type", "PAYMENT_FAILED")
        );

        auditClient.log(
                "PAYMENT_FAILED",
                getConnectedUserEmail(),
                String.format("Paiement échoué — commande: %s, raison: %s",
                        payment.getOrder().getOrderCode(), reason),
                null
        );

        log.warn("Paiement échoué : {} — raison: {}", paymentUuid, reason);

        return toResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse refund(String paymentUuid) {
        Payment payment = getByUuidOrThrow(paymentUuid);

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new BusinessException(
                    "Seul un paiement complété peut être remboursé",
                    "PAYMENT_NOT_COMPLETED",
                    HttpStatus.BAD_REQUEST
            );
        }

        payment.setStatus(PaymentStatus.REFUNDED);
        payment = paymentRepository.save(payment);

        sendPushToUser(
                payment.getOrder().getCustomer().getUser().getId(),
                "Remboursement en cours 🔄",
                String.format("Votre remboursement de %.0f FCFA est en cours",
                        payment.getAmount()),
                Map.of("paymentUuid", payment.getUuid(), "type", "PAYMENT_REFUNDED")
        );

        auditClient.log(
                "PAYMENT_REFUNDED",
                getConnectedUserEmail(),
                "Paiement remboursé — commande : " + payment.getOrder().getOrderCode(),
                null
        );

        log.info("Paiement remboursé : {}", paymentUuid);

        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse findByUuid(String uuid) {
        return toResponse(getByUuidOrThrow(uuid));
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse findByOrderUuid(String orderUuid) {
        return toResponse(paymentRepository.findByOrderUuid(orderUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Paiement", "orderUuid", orderUuid)));
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PaymentResponse> findByAgency(Long agencyId, int page, int size) {
        Page<Payment> payments = paymentRepository.findByOrderAgencyId(
                agencyId, PaginationUtil.build(page, size));
        List<PaymentResponse> content = payments.getContent().stream()
                .map(this::toResponse)
                .toList();
        return PageResponse.from(payments, content);
    }

    @Override
    @Transactional(readOnly = true)
    public Double getTotalCommissions() {
        return paymentRepository.sumTotalCommissions();
    }

    @Override
    @Transactional(readOnly = true)
    public Double getCommissionsByAgency(Long agencyId) {
        return paymentRepository.sumCommissionsByAgency(agencyId);
    }

    private Payment confirmPaymentInternal(Payment payment, String transactionReference) {
        paymentRepository.confirmPayment(
                payment.getUuid(),
                PaymentStatus.COMPLETED,
                transactionReference
        );
        return getByUuidOrThrow(payment.getUuid());
    }

    private Payment getByUuidOrThrow(String uuid) {
        return paymentRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Paiement", "uuid", uuid));
    }

    private Order getOrderOrThrow(String orderUuid) {
        return orderRepository.findByUuid(orderUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Commande", "uuid", orderUuid));
    }

    private void sendPushToUser(Long userId, String title,
                                String body, Map<String, String> data) {
        try {
            List<String> tokens = deviceService.getActiveTokensByUserId(userId);
            for (String token : tokens) {
                notificationClient.sendPush(token, title, body, data);
            }
        } catch (Exception e) {
            log.warn("Impossible d'envoyer la notification : {}", e.getMessage());
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

    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .uuid(payment.getUuid())
                .orderUuid(payment.getOrder().getUuid())
                .orderCode(payment.getOrder().getOrderCode())
                .amount(payment.getAmount())
                .commissionAmount(payment.getCommissionAmount())
                .agencyAmount(payment.getAgencyAmount())
                .method(payment.getMethod())
                .status(payment.getStatus())
                .transactionReference(payment.getTransactionReference())
                .transactionDate(payment.getTransactionDate())
                .failureReason(payment.getFailureReason())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}