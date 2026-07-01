package com.delivery.delivery_api.payment.entity;

import com.delivery.delivery_api.order.entity.Order;
import com.delivery.delivery_api.payment.enums.PaymentMethod;
import com.delivery.delivery_api.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "uuid", name = "uk_payments_uuid"),
                @UniqueConstraint(columnNames = "order_id", name = "uk_payments_order_id")
        },
        indexes = {
                @Index(columnList = "uuid", name = "idx_payments_uuid"),
                @Index(columnList = "order_id", name = "idx_payments_order_id"),
                @Index(columnList = "status", name = "idx_payments_status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String uuid;

    /**
     * Une commande a exactement un paiement (1-1)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    /**
     * Montant total payé par le client (en FCFA)
     */
    @Column(nullable = false)
    private Double amount;

    /**
     * Commission prélevée par la plateforme (en FCFA)
     * Calculée via FeeService.calculateCommission()
     */
    @Column(name = "commission_amount", nullable = false)
    private Double commissionAmount;

    /**
     * Montant reversé à l'agence = amount - commissionAmount
     */
    @Column(name = "agency_amount", nullable = false)
    private Double agencyAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * Référence de transaction retournée par le provider (Airtel, Moov...)
     * Null pour CASH
     */
    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}