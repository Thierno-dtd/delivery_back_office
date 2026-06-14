package com.delivery.delivery_api.user.repository;

import com.delivery.delivery_api.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUuid(String uuid);

    boolean existsByEmail(String email);

    boolean existsByUuid(String uuid);

    @Modifying
    @Query("UPDATE User u SET u.lastLogin = :lastLogin WHERE u.email = :email")
    void updateLastLogin(@Param("email") String email,
                         @Param("lastLogin") LocalDateTime lastLogin);

    @Modifying
    @Query("UPDATE User u SET u.active = :active WHERE u.uuid = :uuid")
    void updateActiveStatus(@Param("uuid") String uuid,
                            @Param("active") boolean active);

    @Modifying
    @Query("UPDATE User u SET u.emailVerified = true WHERE u.email = :email")
    void verifyEmail(@Param("email") String email);

    @Modifying
    @Query("UPDATE User u SET u.password = :password WHERE u.email = :email")
    void updatePassword(@Param("email") String email,
                        @Param("password") String password);
}