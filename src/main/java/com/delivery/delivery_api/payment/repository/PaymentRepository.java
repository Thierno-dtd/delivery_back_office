package com.delivery.delivery_api.payment.repository;

import com.delivery.delivery_api.payment.entity.Payment;
import com.delivery.delivery_api.payment.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByUuid(String uuid);

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByOrderUuid(String orderUuid);

    boolean existsByUuid(String uuid);

    boolean existsByOrderId(Long orderId);

    Page<Payment> findByStatus(PaymentStatus status, Pageable pageable);

    Page<Payment> findByOrderAgencyId(Long agencyId, Pageable pageable);

    Page<Payment> findByOrderAgencyIdAndStatus(Long agencyId,
                                               PaymentStatus status,
                                               Pageable pageable);

    @Modifying
    @Query("UPDATE Payment p SET p.status = :status, " +
            "p.transactionReference = :ref, " +
            "p.transactionDate = CURRENT_TIMESTAMP, " +
            "p.updatedAt = CURRENT_TIMESTAMP " +
            "WHERE p.uuid = :uuid")
    void confirmPayment(@Param("uuid") String uuid,
                        @Param("status") PaymentStatus status,
                        @Param("ref") String transactionReference);

    // Stats commissions - pour le sup admin
    @Query("SELECT COALESCE(SUM(p.commissionAmount), 0) " +
            "FROM Payment p WHERE p.status = 'COMPLETED'")
    Double sumTotalCommissions();

    @Query("SELECT COALESCE(SUM(p.commissionAmount), 0) " +
            "FROM Payment p WHERE p.status = 'COMPLETED' " +
            "AND p.order.agency.id = :agencyId")
    Double sumCommissionsByAgency(@Param("agencyId") Long agencyId);

    @Query("SELECT COALESCE(SUM(p.amount), 0) " +
            "FROM Payment p WHERE p.status = 'COMPLETED' " +
            "AND p.order.agency.id = :agencyId")
    Double sumRevenueByAgency(@Param("agencyId") Long agencyId);
}