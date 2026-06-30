package com.delivery.delivery_api.order.repository;

import com.delivery.delivery_api.order.entity.Order;
import com.delivery.delivery_api.order.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByUuid(String uuid);

    Optional<Order> findByOrderCode(String orderCode);

    boolean existsByUuid(String uuid);

    // Commandes d'un client
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);

    Page<Order> findByCustomerIdAndStatus(Long customerId, OrderStatus status, Pageable pageable);

    // Commandes d'un livreur
    Page<Order> findByDriverId(Long driverId, Pageable pageable);

    Page<Order> findByDriverIdAndStatus(Long driverId, OrderStatus status, Pageable pageable);

    // Commandes d'une agence
    Page<Order> findByAgencyId(Long agencyId, Pageable pageable);

    Page<Order> findByAgencyIdAndStatus(Long agencyId, OrderStatus status, Pageable pageable);

    // Commandes en attente d'une agence (pour l'assignation)
    @Query("SELECT o FROM Order o WHERE o.agency.id = :agencyId " +
            "AND o.status = 'PENDING' ORDER BY o.createdAt ASC")
    Page<Order> findPendingByAgency(@Param("agencyId") Long agencyId, Pageable pageable);

    // Mise à jour statut
    @Modifying
    @Query("UPDATE Order o SET o.status = :status, o.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE o.uuid = :uuid")
    void updateStatus(@Param("uuid") String uuid, @Param("status") OrderStatus status);

    // Assigner un livreur
    @Modifying
    @Query("UPDATE Order o SET o.driver.id = :driverId, o.status = 'ACCEPTED', " +
            "o.updatedAt = CURRENT_TIMESTAMP WHERE o.uuid = :uuid")
    void assignDriver(@Param("uuid") String uuid, @Param("driverId") Long driverId);

    // Stats
    long countByAgencyIdAndStatus(Long agencyId, OrderStatus status);

    long countByCustomerId(Long customerId);
}