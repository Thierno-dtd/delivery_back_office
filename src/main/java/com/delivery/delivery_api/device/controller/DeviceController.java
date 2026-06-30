package com.delivery.delivery_api.device.controller;

import com.delivery.delivery_api.device.dto.request.RegisterDeviceRequest;
import com.delivery.delivery_api.device.dto.response.DeviceTokenResponse;
import com.delivery.delivery_api.device.service.IDeviceService;
import com.delivery.delivery_api.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/devices")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Appareils", description = "Gestion des tokens FCM pour les notifications push")
public class DeviceController {

    private final IDeviceService deviceService;

    @PostMapping("/register")
    @Operation(
            summary = "Enregistrer un token FCM",
            description = "À appeler à chaque démarrage de l'app mobile pour recevoir les notifications push"
    )
    public ResponseEntity<ApiResponse<DeviceTokenResponse>> register(
            @Valid @RequestBody RegisterDeviceRequest request) {
        DeviceTokenResponse response = deviceService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Appareil enregistré", response));
    }

    @DeleteMapping("/unregister")
    @Operation(
            summary = "Désenregistrer un token FCM",
            description = "À appeler au logout pour ne plus recevoir de notifications sur cet appareil"
    )
    public ResponseEntity<ApiResponse<Void>> unregister(@RequestParam String fcmToken) {
        deviceService.unregister(fcmToken);
        return ResponseEntity.ok(ApiResponse.success("Appareil désenregistré"));
    }

    @GetMapping("/me")
    @Operation(summary = "Mes appareils enregistrés")
    public ResponseEntity<ApiResponse<List<DeviceTokenResponse>>> getMyDevices() {
        return ResponseEntity.ok(ApiResponse.success(deviceService.getMyDevices()));
    }
}