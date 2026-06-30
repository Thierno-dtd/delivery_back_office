package com.delivery.delivery_api.customer.entity;

import com.delivery.delivery_api.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "uuid", name = "uk_customers_uuid"),
                @UniqueConstraint(columnNames = "user_id", name = "uk_customers_user_id"),
                @UniqueConstraint(columnNames = "telephone", name = "uk_customers_telephone")
        },
        indexes = {
                @Index(columnList = "uuid", name = "idx_customers_uuid"),
                @Index(columnList = "telephone", name = "idx_customers_telephone")
        }
)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String uuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    private LocalDate birthday;

    @Column(length = 20)
    private String gender;

    @Column(nullable = false, unique = true, length = 20)
    private String telephone;

    @Column(name = "phone_verified", nullable = false)
    @Builder.Default
    private boolean phoneVerified = false;

    @Column(length = 255)
    private String address;

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

    /**
     * Profil considéré complet si nom, prénom et téléphone vérifié sont présents
     * Utilisé pour bloquer la création de commandes tant que le profil n'est pas complet
     */
    public boolean isProfileComplete() {
        return firstName != null && !firstName.isBlank()
                && lastName != null && !lastName.isBlank()
                && phoneVerified;
    }
}