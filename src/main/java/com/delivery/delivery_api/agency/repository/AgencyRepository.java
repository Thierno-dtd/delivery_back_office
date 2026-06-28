package com.delivery.delivery_api.agency.repository;

import com.delivery.delivery_api.agency.entity.Agency;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AgencyRepository extends JpaRepository<Agency, Long> {

    Optional<Agency> findByUuid(String uuid);

    Optional<Agency> findByEmail(String email);

    boolean existsByUuid(String uuid);

    boolean existsByEmail(String email);

    boolean existsByName(String name);

    Page<Agency> findByActive(boolean active, Pageable pageable);

    Page<Agency> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Modifying
    @Query("UPDATE Agency a SET a.active = :active WHERE a.uuid = :uuid")
    void updateActiveStatus(@Param("uuid") String uuid,
                            @Param("active") boolean active);

    long countByActive(boolean active);
}