package com.delivery.delivery_api.fee.repository;

import com.delivery.delivery_api.fee.entity.Fee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeeRepository extends JpaRepository<Fee, Long> {

    Optional<Fee> findByUuid(String uuid);

    Optional<Fee> findByAgencyId(Long agencyId);

    Optional<Fee> findByAgencyUuid(String agencyUuid);

    boolean existsByAgencyId(Long agencyId);

    boolean existsByUuid(String uuid);

    @Modifying
    @Query("UPDATE Fee f SET f.active = :active WHERE f.uuid = :uuid")
    void updateActiveStatus(@Param("uuid") String uuid,
                            @Param("active") boolean active);
}