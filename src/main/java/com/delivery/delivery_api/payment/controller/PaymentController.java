package com.delivery.delivery_api.payment.controller;

import com.delivery.delivery_api.payment.dto.request.CreatePaymentRequest;
import com.delivery.delivery_api.payment.dto.response.PaymentResponse;
import com.delivery.delivery_api.payment.service.IPaymentService;
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
@RequestMapping("/v1/payments")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Paiements", description = "Gestion des paiements et commissions")
public class PaymentController {

    private final IPaymentService paymentService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Initier un paiement",
            description = "La commande doit être en statut DELIVERED. " +
                    "Pour CASH confirmation est automatique."
    )
    public ResponseEntity<ApiResponse<PaymentResponse>> create(
            @Valid @RequestBody CreatePaymentRequest request) {
        PaymentResponse response = paymentService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Paiement initié", response));
    }

    @GetMapping("/{uuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'GESTIONNAIRE', 'CUSTOMER')")
    @Operation(summary = "Détails d'un paiement")
    public ResponseEntity<ApiResponse<PaymentResponse>> findByUuid(
            @PathVariable String uuid) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.findByUuid(uuid)));
    }

    @GetMapping("/order/{orderUuid}")
    @Operation(summary = "Paiement d'une commande")
    public ResponseEntity<ApiResponse<PaymentResponse>> findByOrder(
            @PathVariable String orderUuid) {
        return ResponseEntity.ok(
                ApiResponse.success(paymentService.findByOrderUuid(orderUuid)));
    }

    @GetMapping("/agency/{agencyId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'GESTIONNAIRE')")
    @Operation(summary = "Paiements d'une agence avec pagination")
    public ResponseEntity<ApiResponse<PageResponse<PaymentResponse>>> findByAgency(
            @PathVariable Long agencyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(paymentService.findByAgency(agencyId, page, size)));
    }

    @PatchMapping("/{uuid}/confirm")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'GESTIONNAIRE')")
    @Operation(
            summary = "Confirmer un paiement",
            description = "Valide manuellement un paiement PENDING (Airtel/Moov ou CASH)"
    )
    public ResponseEntity<ApiResponse<PaymentResponse>> confirm(
            @PathVariable String uuid,
            @RequestParam(required = false) String transactionReference) {
        PaymentResponse response = paymentService.confirm(uuid, transactionReference);
        return ResponseEntity.ok(ApiResponse.success("Paiement confirmé", response));
    }

    @PatchMapping("/{uuid}/fail")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'GESTIONNAIRE')")
    @Operation(summary = "Marquer un paiement comme échoué")
    public ResponseEntity<ApiResponse<PaymentResponse>> markAsFailed(
            @PathVariable String uuid,
            @RequestParam String reason) {
        PaymentResponse response = paymentService.markAsFailed(uuid, reason);
        return ResponseEntity.ok(ApiResponse.success("Paiement marqué comme échoué", response));
    }

    @PatchMapping("/{uuid}/refund")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Rembourser un paiement")
    public ResponseEntity<ApiResponse<PaymentResponse>> refund(@PathVariable String uuid) {
        PaymentResponse response = paymentService.refund(uuid);
        return ResponseEntity.ok(ApiResponse.success("Remboursement initié", response));
    }

    @GetMapping("/stats/commissions")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Total des commissions perçues par la plateforme")
    public ResponseEntity<ApiResponse<Double>> getTotalCommissions() {
        return ResponseEntity.ok(
                ApiResponse.success(paymentService.getTotalCommissions()));
    }

    @GetMapping("/stats/commissions/agency/{agencyId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER')")
    @Operation(summary = "Total des commissions perçues sur une agence")
    public ResponseEntity<ApiResponse<Double>> getCommissionsByAgency(
            @PathVariable Long agencyId) {
        return ResponseEntity.ok(
                ApiResponse.success(paymentService.getCommissionsByAgency(agencyId)));
    }
}