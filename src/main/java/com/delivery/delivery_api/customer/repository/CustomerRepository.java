package com.delivery.delivery_api.customer.repository;

import com.delivery.delivery_api.customer.entity.Customer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByUuid(String uuid);

    Optional<Customer> findByUserId(Long userId);

    Optional<Customer> findByUserEmail(String email);

    Optional<Customer> findByTelephone(String telephone);

    boolean existsByUuid(String uuid);

    boolean existsByUserId(Long userId);

    boolean existsByTelephone(String telephone);

    Page<Customer> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName, String lastName, Pageable pageable);

    @Modifying
    @Query("UPDATE Customer c SET c.phoneVerified = true WHERE c.telephone = :telephone")
    void verifyPhone(@Param("telephone") String telephone);

    long count();
}