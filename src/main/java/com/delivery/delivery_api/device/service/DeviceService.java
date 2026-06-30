package com.delivery.delivery_api.device.service;

import com.delivery.delivery_api.device.dto.request.RegisterDeviceRequest;
import com.delivery.delivery_api.device.dto.response.DeviceTokenResponse;
import com.delivery.delivery_api.device.entity.DeviceToken;
import com.delivery.delivery_api.device.repository.DeviceTokenRepository;
import com.delivery.delivery_api.shared.exception.ResourceNotFoundException;
import com.delivery.delivery_api.user.entity.User;
import com.delivery.delivery_api.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService implements IDeviceService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final UserService userService;

    @Override
    @Transactional
    public DeviceTokenResponse register(RegisterDeviceRequest request) {
        User user = userService.getAuthenticatedUser();

        // Si le token existe déjà (même appareil qui se reconnecte) → on le met juste à jour
        DeviceToken existing = deviceTokenRepository.findByFcmToken(request.getFcmToken())
                .orElse(null);

        DeviceToken deviceToken;

        if (existing != null) {
            existing.setActive(true);
            existing.setPlatform(request.getPlatform());
            existing.setDeviceName(request.getDeviceName());
            deviceToken = deviceTokenRepository.save(existing);
            log.info("Token FCM mis à jour pour : {}", user.getEmail());
        } else {
            deviceToken = DeviceToken.builder()
                    .user(user)
                    .fcmToken(request.getFcmToken())
                    .platform(request.getPlatform())
                    .deviceName(request.getDeviceName())
                    .active(true)
                    .build();
            deviceToken = deviceTokenRepository.save(deviceToken);
            log.info("Nouveau token FCM enregistré pour : {} ({})",
                    user.getEmail(), request.getPlatform());
        }

        return toResponse(deviceToken);
    }

    @Override
    @Transactional
    public void unregister(String fcmToken) {
        if (!deviceTokenRepository.existsByFcmToken(fcmToken)) {
            throw new ResourceNotFoundException("Token FCM", "token", fcmToken);
        }
        deviceTokenRepository.deactivateToken(fcmToken);
        log.info("Token FCM désactivé : {}", fcmToken);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceTokenResponse> getMyDevices() {
        User user = userService.getAuthenticatedUser();
        return deviceTokenRepository.findByUserIdAndActive(user.getId(), true).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getActiveTokensByUserId(Long userId) {
        return deviceTokenRepository.findByUserIdAndActive(userId, true).stream()
                .map(DeviceToken::getFcmToken)
                .toList();
    }

    private DeviceTokenResponse toResponse(DeviceToken token) {
        return DeviceTokenResponse.builder()
                .id(token.getId())
                .platform(token.getPlatform())
                .deviceName(token.getDeviceName())
                .active(token.isActive())
                .lastUsedAt(token.getLastUsedAt())
                .createdAt(token.getCreatedAt())
                .build();
    }
}