package com.delivery.delivery_api.driver.repository;

import com.delivery.delivery_api.driver.entity.Driver;
import com.delivery.delivery_api.driver.enums.DriverStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {

    Optional<Driver> findByUuid(String uuid);

    Optional<Driver> findByUserId(Long userId);

    Optional<Driver> findByUserEmail(String email);

    Optional<Driver> findByTelephone(String telephone);

    boolean existsByUuid(String uuid);

    boolean existsByUserId(Long userId);

    boolean existsByTelephone(String telephone);

    Page<Driver> findByAgencyId(Long agencyId, Pageable pageable);

    List<Driver> findByAgencyIdAndStatus(Long agencyId, DriverStatus status);

    Page<Driver> findByAgencyIdAndStatus(Long agencyId, DriverStatus status, Pageable pageable);

    @Modifying
    @Query("UPDATE Driver d SET d.status = :status WHERE d.uuid = :uuid")
    void updateStatus(@Param("uuid") String uuid,
                      @Param("status") DriverStatus status);

    @Modifying
    @Query("UPDATE Driver d SET d.phoneVerified = true WHERE d.telephone = :telephone")
    void verifyPhone(@Param("telephone") String telephone);

    long countByAgencyId(Long agencyId);

    long countByAgencyIdAndStatus(Long agencyId, DriverStatus status);
}