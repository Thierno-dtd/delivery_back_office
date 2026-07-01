package com.delivery.delivery_api.payment.service;

import com.delivery.delivery_api.payment.dto.request.CreatePaymentRequest;
import com.delivery.delivery_api.payment.dto.response.PaymentResponse;
import com.delivery.delivery_api.shared.response.PageResponse;

public interface IPaymentService {

    /**
     * Initie un paiement pour une commande livrée —
     * la commande doit être en statut DELIVERED pour pouvoir payer.
     * Calcule automatiquement la commission via FeeService.
     */
    PaymentResponse create(CreatePaymentRequest request);

    /**
     * Confirme un paiement après validation côté provider (Airtel/Moov) —
     * passe le statut de PENDING à COMPLETED.
     * Pour CASH, confirmation manuelle par le manager/gestionnaire.
     */
    PaymentResponse confirm(String paymentUuid, String transactionReference);

    /**
     * Marque un paiement comme échoué avec une raison
     */
    PaymentResponse markAsFailed(String paymentUuid, String reason);

    /**
     * Rembourse un paiement — passe le statut à REFUNDED
     */
    PaymentResponse refund(String paymentUuid);

    /**
     * Retourne les détails d'un paiement par son uuid
     */
    PaymentResponse findByUuid(String uuid);

    /**
     * Retourne le paiement d'une commande donnée
     */
    PaymentResponse findByOrderUuid(String orderUuid);

    /**
     * Retourne les paiements d'une agence avec pagination —
     * usage manager/gestionnaire
     */
    PageResponse<PaymentResponse> findByAgency(Long agencyId, int page, int size);

    /**
     * Retourne le total des commissions perçues par la plateforme —
     * usage SUPER_ADMIN dashboard
     */
    Double getTotalCommissions();

    /**
     * Retourne le total des commissions perçues sur une agence donnée —
     * usage SUPER_ADMIN
     */
    Double getCommissionsByAgency(Long agencyId);
}