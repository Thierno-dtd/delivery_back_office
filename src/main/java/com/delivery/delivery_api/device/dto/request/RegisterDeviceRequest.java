package com.delivery.delivery_api.device.dto.request;

import com.delivery.delivery_api.device.enums.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RegisterDeviceRequest {

    @NotBlank(message = "Le token FCM est obligatoire")
    private String fcmToken;

    @NotNull(message = "La plateforme est obligatoire")
    private DevicePlatform platform;

    private String deviceName;
}