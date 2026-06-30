package com.delivery.delivery_api.order.controller;

import com.delivery.delivery_api.order.dto.request.AssignDriverRequest;
import com.delivery.delivery_api.order.dto.request.CreateOrderRequest;
import com.delivery.delivery_api.order.dto.request.UpdateOrderStatusRequest;
import com.delivery.delivery_api.order.dto.response.OrderResponse;
import com.delivery.delivery_api.order.dto.response.OrderStatusHistoryResponse;
import com.delivery.delivery_api.order.service.IOrderService;
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
@RequestMapping("/v1/orders")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Commandes", description = "Gestion des commandes de livraison")
public class OrderController {

    private final IOrderService orderService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(
            summary = "Créer une commande",
            description = "Passé par le client depuis l'app mobile"
    )
    public ResponseEntity<ApiResponse<OrderResponse>> create(
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.create(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Commande créée avec succès", response));
    }

    @GetMapping("/{uuid}")
    @Operation(summary = "Détails d'une commande")
    public ResponseEntity<ApiResponse<OrderResponse>> findByUuid(@PathVariable String uuid) {
        return ResponseEntity.ok(ApiResponse.success(orderService.findByUuid(uuid)));
    }

    @GetMapping("/my-orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Mes commandes — client connecté")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getMyOrders(page, size)));
    }

    @GetMapping("/my-deliveries")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(summary = "Mes livraisons — livreur connecté")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getMyDriverOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(orderService.getMyDriverOrders(page, size)));
    }

    @GetMapping("/agency/{agencyId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'GESTIONNAIRE')")
    @Operation(summary = "Commandes d'une agence")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getAgencyOrders(
            @PathVariable Long agencyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(orderService.getAgencyOrders(agencyId, page, size)));
    }

    @GetMapping("/agency/{agencyId}/pending")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'GESTIONNAIRE')")
    @Operation(
            summary = "Commandes en attente d'une agence",
            description = "Commandes PENDING à assigner à un livreur"
    )
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getPendingOrders(
            @PathVariable Long agencyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderService.getPendingOrdersByAgency(agencyId, page, size)));
    }

    @GetMapping("/customer/{customerUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'GESTIONNAIRE')")
    @Operation(summary = "Commandes d'un client — usage admin/manager")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getCustomerOrders(
            @PathVariable String customerUuid,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        orderService.getCustomerOrders(customerUuid, page, size)));
    }

    @PatchMapping("/{uuid}/assign-driver")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'GESTIONNAIRE')")
    @Operation(
            summary = "Assigner un livreur",
            description = "Assigne un livreur AVAILABLE à une commande PENDING"
    )
    public ResponseEntity<ApiResponse<OrderResponse>> assignDriver(
            @PathVariable String uuid,
            @Valid @RequestBody AssignDriverRequest request) {
        OrderResponse response = orderService.assignDriver(uuid, request);
        return ResponseEntity.ok(ApiResponse.success("Livreur assigné avec succès", response));
    }

    @PatchMapping("/{uuid}/status")
    @PreAuthorize("hasAnyRole('DRIVER', 'MANAGER', 'GESTIONNAIRE', 'SUPER_ADMIN')")
    @Operation(
            summary = "Mettre à jour le statut",
            description = "DRIVER : PICKUP → IN_TRANSIT → DELIVERED. MANAGER/GESTIONNAIRE : CANCELLED"
    )
    public ResponseEntity<ApiResponse<OrderResponse>> updateStatus(
            @PathVariable String uuid,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        OrderResponse response = orderService.updateStatus(uuid, request);
        return ResponseEntity.ok(ApiResponse.success("Statut mis à jour", response));
    }

    @PatchMapping("/{uuid}/cancel")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'MANAGER', 'GESTIONNAIRE', 'SUPER_ADMIN')")
    @Operation(
            summary = "Annuler une commande",
            description = "Possible seulement si statut PENDING ou ACCEPTED"
    )
    public ResponseEntity<ApiResponse<OrderResponse>> cancel(
            @PathVariable String uuid,
            @RequestParam(required = false) String reason) {
        OrderResponse response = orderService.cancel(uuid, reason);
        return ResponseEntity.ok(ApiResponse.success("Commande annulée", response));
    }

    @GetMapping("/{uuid}/history")
    @Operation(summary = "Historique des statuts d'une commande")
    public ResponseEntity<ApiResponse<List<OrderStatusHistoryResponse>>> getStatusHistory(
            @PathVariable String uuid) {
        return ResponseEntity.ok(
                ApiResponse.success(orderService.getStatusHistory(uuid)));
    }
}