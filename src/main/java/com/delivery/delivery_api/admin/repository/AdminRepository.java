package com.delivery.delivery_api.admin.repository;

import com.delivery.delivery_api.admin.entity.Admin;
import com.delivery.delivery_api.admin.enums.AdminRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUuid(String uuid);

    Optional<Admin> findByUserId(Long userId);

    Optional<Admin> findByUserEmail(String email);

    boolean existsByUuid(String uuid);

    boolean existsByUserId(Long userId);

    List<Admin> findByAgencyId(Long agencyId);

    Page<Admin> findByRole(AdminRole role, Pageable pageable);

    @Query("SELECT a FROM Admin a WHERE a.role = 'MANAGER' AND a.agencyId = :agencyId")
    Optional<Admin> findManagerByAgencyId(@Param("agencyId") Long agencyId);

    long countByRole(AdminRole role);
}