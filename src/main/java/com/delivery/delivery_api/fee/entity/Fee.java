package com.delivery.delivery_api.fee.entity;

import com.delivery.delivery_api.agency.entity.Agency;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "fees",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "uuid", name = "uk_fees_uuid"),
                @UniqueConstraint(columnNames = "agency_id", name = "uk_fees_agency_id")
        },
        indexes = {
                @Index(columnList = "uuid", name = "idx_fees_uuid"),
                @Index(columnList = "agency_id", name = "idx_fees_agency_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Fee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String uuid;

    /**
     * Une agence a exactement une configuration de frais (1-1)
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false, unique = true)
    private Agency agency;

    /**
     * Seuil de décision entre tarif fixe et pourcentage
     * Si montant_course < thresholdAmount → on applique flatFee
     * Si montant_course >= thresholdAmount → on applique commissionRate
     */
    @Column(name = "threshold_amount", nullable = false)
    private Double thresholdAmount;

    /**
     * Montant fixe prélevé sur les petites courses (< thresholdAmount)
     */
    @Column(name = "flat_fee", nullable = false)
    private Double flatFee;

    /**
     * Pourcentage prélevé sur les grandes courses (>= thresholdAmount)
     */
    @Column(name = "commission_rate", nullable = false)
    private Double commissionRate;

    /**
     * Commission minimale garantie même si le % donne moins
     */
    @Column(name = "min_fee", nullable = false)
    private Double minFee;

    /**
     * Plafond optionnel — null = pas de plafond
     */
    @Column(name = "max_fee")
    private Double maxFee;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

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