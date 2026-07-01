package com.delivery.delivery_api.packages.controller;

import com.delivery.delivery_api.packages.dto.request.CreatePackageRequest;
import com.delivery.delivery_api.packages.dto.request.UpdatePackageRequest;
import com.delivery.delivery_api.packages.dto.response.PackageResponse;
import com.delivery.delivery_api.packages.service.IPackageService;
import com.delivery.delivery_api.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/packages")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Colis", description = "Gestion des colis par commande")
public class PackageController {

    private final IPackageService packageService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Ajouter un colis à une commande",
            description = "Seulement possible si la commande est en statut PENDING"
    )
    public ResponseEntity<ApiResponse<PackageResponse>> create(
            @Valid @RequestBody CreatePackageRequest request) {
        PackageResponse response = packageService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Colis ajouté avec succès", response));
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Détails d'un colis")
    public ResponseEntity<ApiResponse<PackageResponse>> findByUuid(
            @PathVariable String uuid) {
        return ResponseEntity.ok(ApiResponse.success(packageService.findByUuid(uuid)));
    }

    @GetMapping("/order/{orderUuid}")
    @Operation(summary = "Tous les colis d'une commande")
    public ResponseEntity<ApiResponse<List<PackageResponse>>> findByOrder(
            @PathVariable String orderUuid) {
        return ResponseEntity.ok(
                ApiResponse.success(packageService.findByOrder(orderUuid)));
    }

    @PatchMapping("/{uuid}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Modifier un colis",
            description = "Seulement possible si la commande est encore en statut PENDING"
    )
    public ResponseEntity<ApiResponse<PackageResponse>> update(
            @PathVariable String uuid,
            @Valid @RequestBody UpdatePackageRequest request) {
        PackageResponse response = packageService.update(uuid, request);
        return ResponseEntity.ok(ApiResponse.success("Colis mis à jour", response));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER', 'GESTIONNAIRE', 'SUPER_ADMIN')")
    @Operation(
            summary = "Supprimer un colis",
            description = "Seulement possible si la commande est encore en statut PENDING"
    )
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String uuid) {
        packageService.delete(uuid);
        return ResponseEntity.ok(ApiResponse.success("Colis supprimé"));
    }
}