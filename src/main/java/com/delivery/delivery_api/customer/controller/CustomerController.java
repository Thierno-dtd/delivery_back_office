package com.delivery.delivery_api.customer.controller;

import com.delivery.delivery_api.customer.dto.request.CompleteProfileRequest;
import com.delivery.delivery_api.customer.dto.request.UpdateCustomerRequest;
import com.delivery.delivery_api.customer.dto.response.CustomerResponse;
import com.delivery.delivery_api.customer.service.ICustomerService;
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
@RequestMapping("/v1/customers")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Clients", description = "Gestion du profil client")
public class CustomerController {

    private final ICustomerService customerService;

    @PostMapping("/complete-profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Compléter le profil",
            description = "À appeler juste après l'inscription pour renseigner nom, prénom, téléphone"
    )
    public ResponseEntity<ApiResponse<CustomerResponse>> completeProfile(
            @Valid @RequestBody CompleteProfileRequest request) {
        CustomerResponse response = customerService.completeProfile(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Profil complété avec succès", response));
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Mon profil client")
    public ResponseEntity<ApiResponse<CustomerResponse>> getCurrentProfile() {
        return ResponseEntity.ok(ApiResponse.success(customerService.getCurrentProfile()));
    }

    @PatchMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Modifier mon profil client")
    public ResponseEntity<ApiResponse<CustomerResponse>> updateProfile(
            @Valid @RequestBody UpdateCustomerRequest request) {
        CustomerResponse response = customerService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profil mis à jour", response));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'GESTIONNAIRE')")
    @Operation(summary = "Détails d'un client — usage admin/manager")
    public ResponseEntity<ApiResponse<CustomerResponse>> findByUuid(
            @PathVariable String uuid) {
        return ResponseEntity.ok(ApiResponse.success(customerService.findByUuid(uuid)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'GESTIONNAIRE')")
    @Operation(summary = "Rechercher des clients par nom ou prénom")
    public ResponseEntity<ApiResponse<PageResponse<CustomerResponse>>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(customerService.search(query, page, size)));
    }
}