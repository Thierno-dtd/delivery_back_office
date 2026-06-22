package com.delivery.delivery_api.admin.controller;

import com.delivery.delivery_api.admin.dto.request.CreateAdminRequest;
import com.delivery.delivery_api.admin.dto.request.UpdateAdminRequest;
import com.delivery.delivery_api.admin.dto.response.AdminResponse;
import com.delivery.delivery_api.admin.enums.AdminRole;
import com.delivery.delivery_api.admin.service.AdminService;
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
@RequestMapping("/v1/admins")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Administrateurs", description = "Gestion des super admins et managers")
public class AdminController {

    private final AdminService adminService;

    @PostMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Créer un admin", description = "Crée un SUPER_ADMIN ou un MANAGER")
    public ResponseEntity<ApiResponse<AdminResponse>> create(
            @Valid @RequestBody CreateAdminRequest request) {
        AdminResponse response = adminService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Admin créé avec succès", response));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    @Operation(summary = "Détails d'un admin")
    public ResponseEntity<ApiResponse<AdminResponse>> getByUuid(@PathVariable String uuid) {
        AdminResponse response = adminService.findByUuid(uuid);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Liste des admins par rôle", description = "Pagination par rôle")
    public ResponseEntity<ApiResponse<PageResponse<AdminResponse>>> getByRole(
            @RequestParam AdminRole role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<AdminResponse> response = adminService.findByRole(role, page, size);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/agency/{agencyId}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Managers d'une agence")
    public ResponseEntity<ApiResponse<List<AdminResponse>>> getByAgency(
            @PathVariable Long agencyId) {
        List<AdminResponse> response = adminService.findByAgencyId(agencyId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{uuid}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Modifier un admin")
    public ResponseEntity<ApiResponse<AdminResponse>> update(
            @PathVariable String uuid,
            @Valid @RequestBody UpdateAdminRequest request) {
        AdminResponse response = adminService.update(uuid, request);
        return ResponseEntity.ok(ApiResponse.success("Admin mis à jour", response));
    }

    @DeleteMapping("/{uuid}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Désactiver un admin")
    public ResponseEntity<ApiResponse<Void>> deactivate(@PathVariable String uuid) {
        adminService.deactivate(uuid);
        return ResponseEntity.ok(ApiResponse.success("Admin désactivé"));
    }

    @PostMapping("/{uuid}/reset-password")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Réinitialiser le mot de passe",
            description = "Génère un nouveau mot de passe temporaire et l'envoie par email")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@PathVariable String uuid) {
        adminService.resetPassword(uuid);
        return ResponseEntity.ok(ApiResponse.success("Mot de passe réinitialisé et envoyé par email"));
    }
}