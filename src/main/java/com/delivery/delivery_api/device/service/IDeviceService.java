package com.delivery.delivery_api.device.service;

import com.delivery.delivery_api.device.dto.request.RegisterDeviceRequest;
import com.delivery.delivery_api.device.dto.response.DeviceTokenResponse;

import java.util.List;

public interface IDeviceService {

    /**
     * Enregistre ou met à jour le token FCM de l'utilisateur connecté.
     * Si le token existe déjà, met juste à jour son horodatage d'utilisation.
     * Appelé à chaque démarrage de l'app mobile.
     */
    DeviceTokenResponse register(RegisterDeviceRequest request);

    /**
     * Désactive un token FCM — appelé au logout pour ne plus recevoir de push
     * sur cet appareil après déconnexion
     */
    void unregister(String fcmToken);

    /**
     * Retourne tous les tokens actifs de l'utilisateur connecté
     */
    List<DeviceTokenResponse> getMyDevices();

    /**
     * Retourne tous les tokens FCM actifs d'un utilisateur donné —
     * utilisé par les autres modules (order, tracking) pour envoyer des push ciblés
     */
    List<String> getActiveTokensByUserId(Long userId);
}