package com.delivery.delivery_api.tracking.entity;

import com.delivery.delivery_api.driver.entity.Driver;
import com.delivery.delivery_api.order.entity.Order;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "driver_locations",
        indexes = {
                @Index(columnList = "driver_id", name = "idx_driver_locations_driver_id"),
                @Index(columnList = "order_id", name = "idx_driver_locations_order_id"),
                @Index(columnList = "created_at", name = "idx_driver_locations_created_at")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    // Null si le livreur n'est pas en mission (position hors commande)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}