package com.delivery.delivery_api.packages.entity;

import com.delivery.delivery_api.order.entity.Order;
import com.delivery.delivery_api.packages.enums.PackageType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "packages",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "uuid", name = "uk_packages_uuid")
        },
        indexes = {
                @Index(columnList = "uuid", name = "idx_packages_uuid"),
                @Index(columnList = "order_id", name = "idx_packages_order_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Package {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String uuid;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false, length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PackageType type = PackageType.STANDARD;

    @Column(name = "declared_value")
    private Double declaredValue;

    @Column(name = "photo_url", length = 255)
    private String photoUrl;

    @Column
    private Double weight;

    @Column
    private Double width;

    @Column
    private Double height;

    @Column
    private Double length;

    @Column(nullable = false)
    @Builder.Default
    private boolean insured = false;

    @Column(name = "insurance_amount")
    private Double insuranceAmount;

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