package com.delivery.delivery_api.rating.entity;

import com.delivery.delivery_api.order.entity.Order;
import com.delivery.delivery_api.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "ratings",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"order_id", "rater_id"},
                        name = "uk_ratings_order_rater"
                )
        },
        indexes = {
                @Index(columnList = "order_id", name = "idx_ratings_order_id"),
                @Index(columnList = "rated_id", name = "idx_ratings_rated_id"),
                @Index(columnList = "rater_id", name = "idx_ratings_rater_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Rating {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Celui qui donne la note
     * Customer note le Driver, Driver note le Customer
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rater_id", nullable = false)
    private User rater;

    /**
     * Celui qui reçoit la note
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rated_id", nullable = false)
    private User rated;

    /**
     * Note de 1 à 5
     */
    @Column(nullable = false)
    private Integer score;

    @Column(length = 500)
    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}