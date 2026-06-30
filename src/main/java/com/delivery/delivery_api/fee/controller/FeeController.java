package com.delivery.delivery_api.fee.controller;

import com.delivery.delivery_api.fee.dto.request.CreateFeeRequest;
import com.delivery.delivery_api.fee.dto.request.UpdateFeeRequest;
import com.delivery.delivery_api.fee.dto.response.FeeResponse;
import com.delivery.delivery_api.fee.service.IFeeService;
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

@RestController
@RequestMapping("/v1/fees")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Frais & Commissions",
        description = "Configuration des frais et calcul des commissions par agence")
public class FeeController {

    private final IFeeService feeService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Créer une configuration de frais",
            description = "Définit le seuil, le montant fixe et le taux de commission pour une agence"
    )
    public ResponseEntity<ApiResponse<FeeResponse>> create(
            @Valid @RequestBody CreateFeeRequest request) {
        FeeResponse response = feeService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Configuration de frais créée", response));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    @Operation(summary = "Détails d'une configuration de frais par uuid")
    public ResponseEntity<ApiResponse<FeeResponse>> findByUuid(
            @PathVariable String uuid) {
        return ResponseEntity.ok(ApiResponse.success(feeService.findByUuid(uuid)));
    }

    @GetMapping("/agency/{agencyUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    @Operation(summary = "Configuration de frais d'une agence")
    public ResponseEntity<ApiResponse<FeeResponse>> findByAgency(
            @PathVariable String agencyUuid) {
        return ResponseEntity.ok(
                ApiResponse.success(feeService.findByAgencyUuid(agencyUuid)));
    }

    @PatchMapping("/{uuid}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(
            summary = "Modifier une configuration de frais",
            description = "Met à jour seuil, montant fixe, taux ou plafond"
    )
    public ResponseEntity<ApiResponse<FeeResponse>> update(
            @PathVariable String uuid,
            @Valid @RequestBody UpdateFeeRequest request) {
        FeeResponse response = feeService.update(uuid, request);
        return ResponseEntity.ok(ApiResponse.success("Configuration mise à jour", response));
    }

    @PatchMapping("/{uuid}/activate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Activer une configuration de frais")
    public ResponseEntity<ApiResponse<Void>> activate(@PathVariable String uuid) {
        feeService.toggleStatus(uuid, true);
        return ResponseEntity.ok(ApiResponse.success("Configuration activée"));
    }

    @PatchMapping("/{uuid}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Désactiver une configuration de frais")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable String uuid) {
        feeService.toggleStatus(uuid, false);
        return ResponseEntity.ok(ApiResponse.success("Configuration désactivée"));
    }
}