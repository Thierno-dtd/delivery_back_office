package com.delivery.delivery_api.tracking.repository;

import com.delivery.delivery_api.tracking.entity.DriverLocation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DriverLocationRepository extends JpaRepository<DriverLocation, Long> {

    // Dernière position connue d'un livreur
    @Query("SELECT dl FROM DriverLocation dl WHERE dl.driver.id = :driverId " +
            "ORDER BY dl.createdAt DESC")
    List<DriverLocation> findLatestByDriverId(@Param("driverId") Long driverId,
                                              Pageable pageable);

    // Historique de position pour une commande donnée
    List<DriverLocation> findByOrderIdOrderByCreatedAtAsc(Long orderId);

    // Dernière position d'un livreur pour une commande précise
    @Query("SELECT dl FROM DriverLocation dl WHERE dl.driver.id = :driverId " +
            "AND dl.order.id = :orderId ORDER BY dl.createdAt DESC")
    List<DriverLocation> findLatestByDriverAndOrder(@Param("driverId") Long driverId,
                                                    @Param("orderId") Long orderId,
                                                    Pageable pageable);
}