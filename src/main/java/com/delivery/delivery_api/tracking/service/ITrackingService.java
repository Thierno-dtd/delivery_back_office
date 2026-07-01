package com.delivery.delivery_api.tracking.service;

import com.delivery.delivery_api.tracking.dto.request.LocationUpdateRequest;
import com.delivery.delivery_api.tracking.dto.response.LocationResponse;

import java.util.List;

public interface ITrackingService {

    /**
     * Reçoit et persiste la position GPS du livreur connecté —
     * appelé par l'app mobile du livreur toutes les X secondes.
     * Publie aussi la position en temps réel via WebSocket.
     */
    void updateLocation(LocationUpdateRequest request);

    /**
     * Retourne la dernière position connue d'un livreur —
     * utilisé par le manager/gestionnaire pour voir où sont ses livreurs
     */
    LocationResponse getLatestDriverLocation(String driverUuid);

    /**
     * Retourne la dernière position du livreur assigné à une commande —
     * utilisé par le client pour suivre sa livraison en temps réel
     */
    LocationResponse getOrderDriverLocation(String orderUuid);

    /**
     * Retourne l'historique complet des positions GPS pour une commande —
     * utile pour les litiges et l'analytics
     */
    List<LocationResponse> getOrderLocationHistory(String orderUuid);
}