package com.delivery.delivery_api.agency.controller;

import com.delivery.delivery_api.agency.dto.request.CreateAgencyRequest;
import com.delivery.delivery_api.agency.dto.request.UpdateAgencyRequest;
import com.delivery.delivery_api.agency.dto.response.AgencyResponse;
import com.delivery.delivery_api.agency.service.AgencyService;
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

@RestController
@RequestMapping("/v1/agencies")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Agences", description = "Gestion des agences de livraison")
public class AgencyController {

    private final AgencyService agencyService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Créer une agence")
    public ResponseEntity<ApiResponse<AgencyResponse>> create(
            @Valid @RequestBody CreateAgencyRequest request) {
        AgencyResponse response = agencyService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Agence créée avec succès", response));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Liste toutes les agences")
    public ResponseEntity<ApiResponse<PageResponse<AgencyResponse>>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(agencyService.findAll(page, size)));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    @Operation(summary = "Liste les agences actives")
    public ResponseEntity<ApiResponse<PageResponse<AgencyResponse>>> findActive(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(agencyService.findActive(page, size)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Rechercher une agence par nom")
    public ResponseEntity<ApiResponse<PageResponse<AgencyResponse>>> search(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(agencyService.search(name, page, size)));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    @Operation(summary = "Détails d'une agence")
    public ResponseEntity<ApiResponse<AgencyResponse>> findByUuid(
            @PathVariable String uuid) {
        return ResponseEntity.ok(ApiResponse.success(agencyService.findByUuid(uuid)));
    }

    @PatchMapping("/{uuid}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Modifier une agence")
    public ResponseEntity<ApiResponse<AgencyResponse>> update(
            @PathVariable String uuid,
            @Valid @RequestBody UpdateAgencyRequest request) {
        AgencyResponse response = agencyService.update(uuid, request);
        return ResponseEntity.ok(ApiResponse.success("Agence mise à jour", response));
    }

    @PatchMapping("/{uuid}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Activer une agence")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable String uuid) {
        agencyService.toggleStatus(uuid, true);
        return ResponseEntity.ok(ApiResponse.success("Agence activée"));
    }

    @PatchMapping("/{uuid}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Désactiver une agence")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable String uuid) {
        agencyService.toggleStatus(uuid, false);
        return ResponseEntity.ok(ApiResponse.success("Agence désactivée"));
    }
}