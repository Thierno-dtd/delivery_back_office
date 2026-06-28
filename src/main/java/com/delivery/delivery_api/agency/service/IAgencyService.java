package com.delivery.delivery_api.agency.service;

import com.delivery.delivery_api.agency.dto.request.CreateAgencyRequest;
import com.delivery.delivery_api.agency.dto.request.UpdateAgencyRequest;
import com.delivery.delivery_api.agency.dto.response.AgencyResponse;
import com.delivery.delivery_api.agency.entity.Agency;
import com.delivery.delivery_api.shared.response.PageResponse;

public interface IAgencyService {

    /**
     * Crée une nouvelle agence de livraison
     */
    AgencyResponse create(CreateAgencyRequest request);

    /**
     * Retourne les détails d'une agence par son uuid
     */
    AgencyResponse findByUuid(String uuid);

    /**
     * Retourne la liste paginée de toutes les agences
     */
    PageResponse<AgencyResponse> findAll(int page, int size);

    /**
     * Retourne la liste paginée des agences actives uniquement
     */
    PageResponse<AgencyResponse> findActive(int page, int size);

    /**
     * Recherche des agences par nom (recherche partielle insensible à la casse)
     */
    PageResponse<AgencyResponse> search(String name, int page, int size);

    /**
     * Met à jour les informations d'une agence
     */
    AgencyResponse update(String uuid, UpdateAgencyRequest request);

    /**
     * Active ou désactive une agence
     */
    void toggleStatus(String uuid, boolean active);

    /**
     * Retourne l'entité Agency par uuid ou lève une exception — utilisé par d'autres modules
     */
    Agency getByUuidOrThrow(String uuid);

    /**
     * Retourne l'entité Agency par id ou lève une exception — utilisé par d'autres modules
     */
    Agency getByIdOrThrow(Long id);

    /**
     * Vérifie qu'une agence existe par son id — lève une exception si non trouvée
     */
    void validateAgencyExists(Long agencyId);

    /**
     * Vérifie qu'une agence existe et est active — lève une exception sinon
     */
    void validateAgencyActive(Long agencyId);

    /**
     * Retourne le nombre total d'agences actives
     */
    long countActive();
}