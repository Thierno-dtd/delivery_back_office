package com.delivery.delivery_api.zone.service;

import com.delivery.delivery_api.shared.response.PageResponse;
import com.delivery.delivery_api.zone.dto.request.CreateZoneRequest;
import com.delivery.delivery_api.zone.dto.request.UpdateZoneRequest;
import com.delivery.delivery_api.zone.dto.response.ZoneResponse;
import com.delivery.delivery_api.zone.entity.Zone;

import java.util.List;

public interface IZoneService {

    /**
     * Crée une nouvelle zone de couverture pour une agence
     */
    ZoneResponse create(CreateZoneRequest request);

    /**
     * Retourne les détails d'une zone par son uuid
     */
    ZoneResponse findByUuid(String uuid);

    /**
     * Retourne la liste paginée des zones d'une agence donnée
     */
    PageResponse<ZoneResponse> findByAgency(Long agencyId, int page, int size);

    /**
     * Retourne toutes les zones actives d'une agence — utilisé pour les commandes
     */
    List<ZoneResponse> findActiveByAgency(Long agencyId);

    /**
     * Retourne la liste paginée de toutes les zones
     */
    PageResponse<ZoneResponse> findAll(int page, int size);

    /**
     * Met à jour les informations d'une zone (nom, ville, tarif de base)
     */
    ZoneResponse update(String uuid, UpdateZoneRequest request);

    /**
     * Active ou désactive une zone
     */
    void toggleStatus(String uuid, boolean active);

    /**
     * Retourne l'entité Zone par uuid ou lève une exception — utilisé par d'autres modules
     */
    Zone getByUuidOrThrow(String uuid);

    /**
     * Retourne l'entité Zone par id ou lève une exception — utilisé par d'autres modules
     */
    Zone getByIdOrThrow(Long id);

    /**
     * Vérifie qu'une zone est active — lève une exception sinon
     */
    void validateZoneActive(Long zoneId);
}