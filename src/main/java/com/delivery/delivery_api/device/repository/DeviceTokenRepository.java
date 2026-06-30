package com.delivery.delivery_api.device.repository;

import com.delivery.delivery_api.device.entity.DeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByFcmToken(String fcmToken);

    List<DeviceToken> findByUserIdAndActive(Long userId, boolean active);

    List<DeviceToken> findByUserId(Long userId);

    boolean existsByFcmToken(String fcmToken);

    @Modifying
    @Query("UPDATE DeviceToken d SET d.active = false WHERE d.fcmToken = :fcmToken")
    void deactivateToken(@Param("fcmToken") String fcmToken);

    @Modifying
    @Query("UPDATE DeviceToken d SET d.lastUsedAt = CURRENT_TIMESTAMP WHERE d.fcmToken = :fcmToken")
    void touchLastUsed(@Param("fcmToken") String fcmToken);

    @Modifying
    @Query("UPDATE DeviceToken d SET d.active = false WHERE d.user.id = :userId AND d.fcmToken != :currentToken")
    void deactivateOtherTokens(@Param("userId") Long userId,
                               @Param("currentToken") String currentToken);
}