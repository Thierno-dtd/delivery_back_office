package com.delivery.delivery_api.driver.entity;

import com.delivery.delivery_api.agency.entity.Agency;
import com.delivery.delivery_api.driver.enums.DriverStatus;
import com.delivery.delivery_api.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "drivers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "uuid", name = "uk_drivers_uuid"),
                @UniqueConstraint(columnNames = "user_id", name = "uk_drivers_user_id"),
                @UniqueConstraint(columnNames = "telephone", name = "uk_drivers_telephone")
        },
        indexes = {
                @Index(columnList = "uuid", name = "idx_drivers_uuid"),
                @Index(columnList = "agency_id", name = "idx_drivers_agency_id"),
                @Index(columnList = "status", name = "idx_drivers_status")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    private LocalDate birthday;

    @Column(length = 255)
    private String picture;

    @Column(name = "identity_doc", length = 255)
    private String identityDoc;

    @Column(length = 255)
    private String address;

    @Column(nullable = false, unique = true, length = 20)
    private String telephone;

    @Column(name = "phone_verified", nullable = false)
    @Builder.Default
    private boolean phoneVerified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private DriverStatus status = DriverStatus.OFFLINE;

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


    public boolean isAvailable() {
        return this.status == DriverStatus.AVAILABLE;
    }

    public boolean isProfileComplete() {
        return identityDoc != null && !identityDoc.isBlank() && phoneVerified;
    }
}