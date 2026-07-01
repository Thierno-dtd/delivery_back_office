package com.delivery.delivery_api.tracking.controller;

import com.delivery.delivery_api.shared.response.ApiResponse;
import com.delivery.delivery_api.tracking.dto.request.LocationUpdateRequest;
import com.delivery.delivery_api.tracking.dto.response.LocationResponse;
import com.delivery.delivery_api.tracking.service.ITrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/tracking")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Tracking", description = "Suivi GPS des livreurs en temps réel")
public class TrackingController {

    private final ITrackingService trackingService;

    @PostMapping("/location")
    @PreAuthorize("hasRole('DRIVER')")
    @Operation(
            summary = "Mettre à jour ma position GPS",
            description = "Appelé par l'app mobile du livreur toutes les 5-10 secondes"
    )
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @Valid @RequestBody LocationUpdateRequest request) {
        trackingService.updateLocation(request);
        return ResponseEntity.ok(ApiResponse.success("Position mise à jour"));
    }

    @GetMapping("/driver/{driverUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'GESTIONNAIRE')")
    @Operation(summary = "Dernière position connue d'un livreur")
    public ResponseEntity<ApiResponse<LocationResponse>> getDriverLocation(
            @PathVariable String driverUuid) {
        return ResponseEntity.ok(
                ApiResponse.success(trackingService.getLatestDriverLocation(driverUuid)));
    }

    @GetMapping("/order/{orderUuid}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'SUPER_ADMIN', 'MANAGER', 'GESTIONNAIRE')")
    @Operation(
            summary = "Position du livreur pour une commande",
            description = "Utilisé par le client pour suivre sa livraison"
    )
    public ResponseEntity<ApiResponse<LocationResponse>> getOrderDriverLocation(
            @PathVariable String orderUuid) {
        return ResponseEntity.ok(
                ApiResponse.success(trackingService.getOrderDriverLocation(orderUuid)));
    }

    @GetMapping("/order/{orderUuid}/history")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'MANAGER', 'GESTIONNAIRE')")
    @Operation(
            summary = "Historique GPS d'une commande",
            description = "Utile pour les litiges et l'analytics"
    )
    public ResponseEntity<ApiResponse<List<LocationResponse>>> getOrderHistory(
            @PathVariable String orderUuid) {
        return ResponseEntity.ok(
                ApiResponse.success(trackingService.getOrderLocationHistory(orderUuid)));
    }

    // ===== WEBSOCKET =====

    /**
     * Point d'entrée WebSocket pour la mise à jour de position.
     * Le client mobile envoie vers : /app/location.update
     * Les abonnés reçoivent sur : /topic/tracking.driver.{driverUuid}
     *                          et : /topic/tracking.order.{orderUuid}
     *
     * Alternative au endpoint REST — utile si le livreur
     * veut garder une connexion WebSocket persistante
     * plutôt que de faire des requêtes HTTP répétées.
     */
    @MessageMapping("/location.update")
    public void handleWebSocketLocationUpdate(
            @Payload LocationUpdateRequest request) {
        trackingService.updateLocation(request);
    }
}