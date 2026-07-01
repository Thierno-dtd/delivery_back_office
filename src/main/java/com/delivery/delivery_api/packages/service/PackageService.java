package com.delivery.delivery_api.packages.service;

import com.delivery.delivery_api.order.entity.Order;
import com.delivery.delivery_api.order.enums.OrderStatus;
import com.delivery.delivery_api.order.repository.OrderRepository;
import com.delivery.delivery_api.packages.dto.request.CreatePackageRequest;
import com.delivery.delivery_api.packages.dto.request.UpdatePackageRequest;
import com.delivery.delivery_api.packages.dto.response.PackageResponse;
import com.delivery.delivery_api.packages.entity.Package;
import com.delivery.delivery_api.packages.repository.PackageRepository;
import com.delivery.delivery_api.shared.audit.AuditClient;
import com.delivery.delivery_api.shared.exception.BusinessException;
import com.delivery.delivery_api.shared.exception.ResourceNotFoundException;
import com.delivery.delivery_api.shared.utils.KeyGeneratorUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PackageService implements IPackageService {

    private final PackageRepository packageRepository;
    private final OrderRepository orderRepository;
    private final AuditClient auditClient;

    @Override
    @Transactional
    public PackageResponse create(CreatePackageRequest request) {
        Order order = getOrderOrThrow(request.getOrderUuid());

        // On ne peut ajouter un colis que sur une commande PENDING
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(
                    "Impossible d'ajouter un colis — la commande n'est plus en attente",
                    "ORDER_NOT_PENDING",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Valider l'assurance
        if (request.isInsured() && request.getInsuranceAmount() == null) {
            throw new BusinessException(
                    "Le montant de l'assurance est obligatoire si le colis est assuré",
                    "INSURANCE_AMOUNT_REQUIRED",
                    HttpStatus.BAD_REQUEST
            );
        }

        Package pkg = Package.builder()
                .uuid(KeyGeneratorUtil.generateRandomToken(16))
                .order(order)
                .description(request.getDescription())
                .type(request.getType())
                .declaredValue(request.getDeclaredValue())
                .photoUrl(request.getPhotoUrl())
                .weight(request.getWeight())
                .width(request.getWidth())
                .height(request.getHeight())
                .length(request.getLength())
                .insured(request.isInsured())
                .insuranceAmount(request.isInsured() ? request.getInsuranceAmount() : null)
                .build();

        pkg = packageRepository.save(pkg);

        auditClient.log(
                "PACKAGE_ADDED",
                getConnectedUserEmail(),
                String.format("Colis ajouté à la commande %s — type: %s",
                        order.getOrderCode(), request.getType()),
                null
        );

        log.info("Colis ajouté à la commande : {}", order.getOrderCode());

        return toResponse(pkg);
    }

    @Override
    @Transactional(readOnly = true)
    public PackageResponse findByUuid(String uuid) {
        return toResponse(getByUuidOrThrow(uuid));
    }

    @Override
    @Transactional(readOnly = true)
    public List<PackageResponse> findByOrder(String orderUuid) {
        return packageRepository.findByOrderUuid(orderUuid).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public PackageResponse update(String uuid, UpdatePackageRequest request) {
        Package pkg = getByUuidOrThrow(uuid);

        if (pkg.getOrder().getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(
                    "Impossible de modifier un colis — la commande n'est plus en attente",
                    "ORDER_NOT_PENDING",
                    HttpStatus.BAD_REQUEST
            );
        }

        // Valider assurance
        boolean finalInsured = request.getInsured() != null
                ? request.getInsured() : pkg.isInsured();

        if (finalInsured && request.getInsuranceAmount() == null
                && pkg.getInsuranceAmount() == null) {
            throw new BusinessException(
                    "Le montant de l'assurance est obligatoire si le colis est assuré",
                    "INSURANCE_AMOUNT_REQUIRED",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (request.getDescription() != null) pkg.setDescription(request.getDescription());
        if (request.getType() != null) pkg.setType(request.getType());
        if (request.getDeclaredValue() != null) pkg.setDeclaredValue(request.getDeclaredValue());
        if (request.getPhotoUrl() != null) pkg.setPhotoUrl(request.getPhotoUrl());
        if (request.getWeight() != null) pkg.setWeight(request.getWeight());
        if (request.getWidth() != null) pkg.setWidth(request.getWidth());
        if (request.getHeight() != null) pkg.setHeight(request.getHeight());
        if (request.getLength() != null) pkg.setLength(request.getLength());
        if (request.getInsured() != null) pkg.setInsured(request.getInsured());
        if (request.getInsuranceAmount() != null)
            pkg.setInsuranceAmount(request.getInsuranceAmount());

        pkg = packageRepository.save(pkg);

        log.info("Colis mis à jour : {}", uuid);

        return toResponse(pkg);
    }

    @Override
    @Transactional
    public void delete(String uuid) {
        Package pkg = getByUuidOrThrow(uuid);

        if (pkg.getOrder().getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(
                    "Impossible de supprimer un colis — la commande n'est plus en attente",
                    "ORDER_NOT_PENDING",
                    HttpStatus.BAD_REQUEST
            );
        }

        packageRepository.delete(pkg);

        auditClient.log(
                "PACKAGE_DELETED",
                getConnectedUserEmail(),
                "Colis supprimé de la commande : " + pkg.getOrder().getOrderCode(),
                null
        );

        log.info("Colis supprimé : {}", uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByOrder(String orderUuid) {
        Order order = getOrderOrThrow(orderUuid);
        return packageRepository.countByOrderId(order.getId());
    }

    private Package getByUuidOrThrow(String uuid) {
        return packageRepository.findByUuid(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("Colis", "uuid", uuid));
    }

    private Order getOrderOrThrow(String orderUuid) {
        return orderRepository.findByUuid(orderUuid)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Commande", "uuid", orderUuid));
    }

    private String getConnectedUserEmail() {
        try {
            return SecurityContextHolder.getContext()
                    .getAuthentication().getName();
        } catch (Exception e) {
            return "system";
        }
    }

    private PackageResponse toResponse(Package pkg) {
        return PackageResponse.builder()
                .uuid(pkg.getUuid())
                .orderUuid(pkg.getOrder().getUuid())
                .orderCode(pkg.getOrder().getOrderCode())
                .description(pkg.getDescription())
                .type(pkg.getType())
                .declaredValue(pkg.getDeclaredValue())
                .photoUrl(pkg.getPhotoUrl())
                .weight(pkg.getWeight())
                .width(pkg.getWidth())
                .height(pkg.getHeight())
                .length(pkg.getLength())
                .insured(pkg.isInsured())
                .insuranceAmount(pkg.getInsuranceAmount())
                .createdAt(pkg.getCreatedAt())
                .updatedAt(pkg.getUpdatedAt())
                .build();
    }
}