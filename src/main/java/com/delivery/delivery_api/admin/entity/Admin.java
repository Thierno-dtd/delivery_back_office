package com.delivery.delivery_api.admin.entity;

import com.delivery.delivery_api.admin.enums.AdminRole;
import com.delivery.delivery_api.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "admins",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "uuid", name = "uk_admins_uuid"),
                @UniqueConstraint(columnNames = "user_id", name = "uk_admins_user_id")
        },
        indexes = {
                @Index(columnList = "uuid", name = "idx_admins_uuid"),
                @Index(columnList = "agency_id", name = "idx_admins_agency_id")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Admin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminRole role;

    // Null si SUPER_ADMIN, renseigné si MANAGER
    @Column(name = "agency_id")
    private Long agencyId;

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

    // ===== HELPERS =====

    public boolean isSuperAdmin() {
        return this.role == AdminRole.SUPER_ADMIN;
    }

    public boolean isManager() {
        return this.role == AdminRole.MANAGER;
    }
}