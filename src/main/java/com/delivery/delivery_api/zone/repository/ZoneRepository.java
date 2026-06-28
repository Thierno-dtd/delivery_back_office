package com.delivery.delivery_api.zone.repository;

import com.delivery.delivery_api.zone.entity.Zone;
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
public interface ZoneRepository extends JpaRepository<Zone, Long> {

    Optional<Zone> findByUuid(String uuid);

    boolean existsByUuid(String uuid);

    boolean existsByNameAndAgencyId(String name, Long agencyId);

    Page<Zone> findByAgencyId(Long agencyId, Pageable pageable);

    List<Zone> findByAgencyIdAndActive(Long agencyId, boolean active);

    Page<Zone> findByActive(boolean active, Pageable pageable);

    @Modifying
    @Query("UPDATE Zone z SET z.active = :active WHERE z.uuid = :uuid")
    void updateActiveStatus(@Param("uuid") String uuid,
                            @Param("active") boolean active);

    long countByAgencyIdAndActive(Long agencyId, boolean active);
}