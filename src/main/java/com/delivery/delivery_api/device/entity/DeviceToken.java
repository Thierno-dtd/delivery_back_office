package com.delivery.delivery_api.device.entity;

import com.delivery.delivery_api.device.enums.DevicePlatform;
import com.delivery.delivery_api.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "device_tokens",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "fcm_token", name = "uk_device_tokens_fcm_token")
        },
        indexes = {
                @Index(columnList = "user_id", name = "idx_device_tokens_user_id"),
                @Index(columnList = "fcm_token", name = "idx_device_tokens_fcm_token")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "fcm_token", nullable = false, unique = true, length = 255)
    private String fcmToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DevicePlatform platform;

    @Column(name = "device_name", length = 100)
    private String deviceName;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.lastUsedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}