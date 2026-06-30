package com.delivery.delivery_api.device.dto.response;

import com.delivery.delivery_api.device.enums.DevicePlatform;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeviceTokenResponse {

    private Long id;
    private DevicePlatform platform;
    private String deviceName;
    private boolean active;
    private LocalDateTime lastUsedAt;
    private LocalDateTime createdAt;
}