package com.delivery.delivery_api.zone.controller;

import com.delivery.delivery_api.shared.response.ApiResponse;
import com.delivery.delivery_api.shared.response.PageResponse;
import com.delivery.delivery_api.zone.dto.request.CreateZoneRequest;
import com.delivery.delivery_api.zone.dto.request.UpdateZoneRequest;
import com.delivery.delivery_api.zone.dto.response.ZoneResponse;
import com.delivery.delivery_api.zone.service.ZoneService;
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
@RequestMapping("/v1/zones")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Zones", description = "Gestion des zones de couverture par agence")
public class ZoneController {

    private final ZoneService zoneService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Créer une zone")
    public ResponseEntity<ApiResponse<ZoneResponse>> create(
            @Valid @RequestBody CreateZoneRequest request) {
        ZoneResponse response = zoneService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Zone créée avec succès", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Toutes les zones")
    public ResponseEntity<ApiResponse<PageResponse<ZoneResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(zoneService.findAll(page, size)));
    }

    @GetMapping("/agency/{agencyId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    @Operation(summary = "Zones d'une agence (paginées)")
    public ResponseEntity<ApiResponse<PageResponse<ZoneResponse>>> findByAgency(
            @PathVariable Long agencyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(zoneService.findByAgency(agencyId, page, size)));
    }

    @GetMapping("/agency/{agencyId}/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'CUSTOMER')")
    @Operation(summary = "Zones actives d'une agence")
    public ResponseEntity<ApiResponse<List<ZoneResponse>>> findActiveByAgency(
            @PathVariable Long agencyId) {
        return ResponseEntity.ok(
                ApiResponse.success(zoneService.findActiveByAgency(agencyId)));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    @Operation(summary = "Détails d'une zone")
    public ResponseEntity<ApiResponse<ZoneResponse>> findByUuid(
            @PathVariable String uuid) {
        return ResponseEntity.ok(ApiResponse.success(zoneService.findByUuid(uuid)));
    }

    @PatchMapping("/{uuid}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Modifier une zone")
    public ResponseEntity<ApiResponse<ZoneResponse>> update(
            @PathVariable String uuid,
            @Valid @RequestBody UpdateZoneRequest request) {
        ZoneResponse response = zoneService.update(uuid, request);
        return ResponseEntity.ok(ApiResponse.success("Zone mise à jour", response));
    }

    @PatchMapping("/{uuid}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Activer une zone")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable String uuid) {
        zoneService.toggleStatus(uuid, true);
        return ResponseEntity.ok(ApiResponse.success("Zone activée"));
    }

    @PatchMapping("/{uuid}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Désactiver une zone")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable String uuid) {
        zoneService.toggleStatus(uuid, false);
        return ResponseEntity.ok(ApiResponse.success("Zone désactivée"));
    }
}