package com.delivery.delivery_api.packages.repository;

import com.delivery.delivery_api.packages.entity.Package;
import com.delivery.delivery_api.packages.enums.PackageType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PackageRepository extends JpaRepository<Package, Long> {

    Optional<Package> findByUuid(String uuid);

    boolean existsByUuid(String uuid);

    List<Package> findByOrderId(Long orderId);

    List<Package> findByOrderUuid(String orderUuid);

    List<Package> findByOrderIdAndType(Long orderId, PackageType type);

    long countByOrderId(Long orderId);
}