package com.delivery.delivery_api.order.entity;

import com.delivery.delivery_api.agency.entity.Agency;
import com.delivery.delivery_api.customer.entity.Customer;
import com.delivery.delivery_api.driver.entity.Driver;
import com.delivery.delivery_api.order.enums.OrderStatus;
import com.delivery.delivery_api.zone.entity.Zone;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orders",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "uuid", name = "uk_orders_uuid"),
                @UniqueConstraint(columnNames = "order_code", name = "uk_orders_order_code")
        },
        indexes = {
                @Index(columnList = "uuid", name = "idx_orders_uuid"),
                @Index(columnList = "customer_id", name = "idx_orders_customer_id"),
                @Index(columnList = "driver_id", name = "idx_orders_driver_id"),
                @Index(columnList = "agency_id", name = "idx_orders_agency_id"),
                @Index(columnList = "status", name = "idx_orders_status"),
                @Index(columnList = "created_at", name = "idx_orders_created_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String uuid;

    @Column(name = "order_code", nullable = false, unique = true, length = 20)
    private String orderCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zone_id")
    private Zone zone;

    @Column(name = "departure_address", nullable = false, length = 255)
    private String departureAddress;

    @Column(name = "departure_lat")
    private Double departureLat;

    @Column(name = "departure_lng")
    private Double departureLng;

    @Column(name = "arrival_address", nullable = false, length = 255)
    private String arrivalAddress;

    @Column(name = "arrival_lat")
    private Double arrivalLat;

    @Column(name = "arrival_lng")
    private Double arrivalLng;

    @Column(name = "estimated_price")
    private Double estimatedPrice;

    @Column(name = "final_price")
    private Double finalPrice;

    @Column(name = "commission_amount")
    private Double commissionAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "cancellation_reason", length = 255)
    private String cancellationReason;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

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

    public boolean isCancellable() {
        return this.status == OrderStatus.PENDING
                || this.status == OrderStatus.ACCEPTED;
    }

    public boolean isActive() {
        return this.status != OrderStatus.DELIVERED
                && this.status != OrderStatus.CANCELLED;
    }
}