package com.delivery.delivery_api.agency.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "agencies",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "uuid", name = "uk_agencies_uuid"),
                @UniqueConstraint(columnNames = "email", name = "uk_agencies_email")
        },
        indexes = {
                @Index(columnList = "uuid", name = "idx_agencies_uuid"),
                @Index(columnList = "active", name = "idx_agencies_active")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Agency {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String uuid;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 255)
    private String slogan;

    @Column(length = 255)
    private String address;

    @Column(length = 20)
    private String telephone;

    @Column(unique = true, length = 150)
    private String email;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

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