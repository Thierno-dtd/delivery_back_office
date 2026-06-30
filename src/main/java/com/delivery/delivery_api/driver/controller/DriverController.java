package com.delivery.delivery_api.driver.controller;

import com.delivery.delivery_api.driver.dto.request.CreateDriverRequest;
import com.delivery.delivery_api.driver.dto.request.UpdateDriverRequest;
import com.delivery.delivery_api.driver.dto.response.DriverResponse;
import com.delivery.delivery_api.driver.enums.DriverStatus;
import com.delivery.delivery_api.driver.service.IDriverService;
import com.delivery.delivery_api.shared.response.ApiResponse;
import com.delivery.delivery_api.shared.response.PageResponse;
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
@RequestMapping("/v1/drivers")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Livreurs", description = "Gestion des livreurs par agence")
public class DriverController {

    private final IDriverService driverService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'GESTIONNAIRE')")
    @Operation(
            summary = "Créer un livreur",
            description = "Identifiants générés automatiquement et envoyés par email"
    )
    public ResponseEntity<ApiResponse<DriverResponse>> create(
            @Valid @RequestBody CreateDriverRequest request) {
        DriverResponse response = driverService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Livreur créé avec succès", response));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Mon profil livreur")
    public ResponseEntity<ApiResponse<DriverResponse>> getCurrentProfile() {
        return ResponseEntity.ok(ApiResponse.success(driverService.getCurrentProfile()));
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Modifier mon profil (photo, documents)")
    public ResponseEntity<ApiResponse<DriverResponse>> updateOwnProfile(
            @Valid @RequestBody UpdateDriverRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Profil mis à jour", driverService.updateOwnProfile(request)));
    }

    @PatchMapping("/me/status")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(
            summary = "Changer mon statut",
            description = "AVAILABLE, BUSY ou OFFLINE — utilisé par l'app mobile du livreur"
    )
    public ResponseEntity<ApiResponse<Void>> updateStatus(
            @RequestParam DriverStatus status) {
        driverService.updateStatus(status);
        return ResponseEntity.ok(ApiResponse.success("Statut mis à jour"));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'GESTIONNAIRE')")
    @Operation(summary = "Détails d'un livreur")
    public ResponseEntity<ApiResponse<DriverResponse>> findByUuid(
            @PathVariable String uuid) {
        return ResponseEntity.ok(ApiResponse.success(driverService.findByUuid(uuid)));
    }

    @GetMapping("/agency/{agencyId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'GESTIONNAIRE')")
    @Operation(summary = "Liste paginée des livreurs d'une agence")
    public ResponseEntity<ApiResponse<PageResponse<DriverResponse>>> findByAgency(
            @PathVariable Long agencyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(driverService.findByAgency(agencyId, page, size)));
    }

    @GetMapping("/agency/{agencyId}/available")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'GESTIONNAIRE')")
    @Operation(summary = "Livreurs disponibles d'une agence — pour assignation de commande")
    public ResponseEntity<ApiResponse<List<DriverResponse>>> findAvailableByAgency(
            @PathVariable Long agencyId) {
        return ResponseEntity.ok(
                ApiResponse.success(driverService.findAvailableByAgency(agencyId)));
    }

    @PatchMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'GESTIONNAIRE')")
    @Operation(summary = "Modifier un livreur")
    public ResponseEntity<ApiResponse<DriverResponse>> update(
            @PathVariable String uuid,
            @Valid @RequestBody UpdateDriverRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Livreur mis à jour", driverService.update(uuid, request)));
    }
}