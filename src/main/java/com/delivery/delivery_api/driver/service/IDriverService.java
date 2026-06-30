package com.delivery.delivery_api.driver.service;

import com.delivery.delivery_api.driver.dto.request.CreateDriverRequest;
import com.delivery.delivery_api.driver.dto.request.UpdateDriverRequest;
import com.delivery.delivery_api.driver.dto.response.DriverResponse;
import com.delivery.delivery_api.driver.entity.Driver;
import com.delivery.delivery_api.driver.enums.DriverStatus;
import com.delivery.delivery_api.shared.response.PageResponse;

import java.util.List;

public interface IDriverService {

    /**
     * Crée un nouveau livreur pour une agence — sans mot de passe,
     * un mot de passe temporaire est généré et envoyé par email
     */
    DriverResponse create(CreateDriverRequest request);

    /**
     * Retourne les détails d'un livreur par son uuid
     */
    DriverResponse findByUuid(String uuid);

    /**
     * Retourne le profil du livreur actuellement connecté
     */
    DriverResponse getCurrentProfile();

    /**
     * Retourne la liste paginée des livreurs d'une agence
     */
    PageResponse<DriverResponse> findByAgency(Long agencyId, int page, int size);

    /**
     * Retourne les livreurs disponibles d'une agence — utilisé pour l'assignation de commandes
     */
    List<DriverResponse> findAvailableByAgency(Long agencyId);

    /**
     * Met à jour les informations d'un livreur
     */
    DriverResponse update(String uuid, UpdateDriverRequest request);

    /**
     * Met à jour le profil du livreur connecté (photo, documents)
     */
    DriverResponse updateOwnProfile(UpdateDriverRequest request);

    /**
     * Change le statut d'un livreur (AVAILABLE, BUSY, OFFLINE) —
     * appelé par le livreur lui-même depuis l'app mobile
     */
    void updateStatus(DriverStatus status);

    /**
     * Marque le téléphone d'un livreur comme vérifié (après OTP)
     */
    void verifyPhone(String telephone);

    /**
     * Retourne l'entité Driver du livreur connecté — utilisé par le module order/tracking
     */
    Driver getAuthenticatedDriver();

    /**
     * Retourne l'entité Driver par uuid ou lève une exception — utilisé par d'autres modules
     */
    Driver getByUuidOrThrow(String uuid);

    /**
     * Vérifie qu'un livreur appartient bien à une agence donnée —
     * utilisé pour les contrôles d'accès manager/gestionnaire
     */
    void validateDriverBelongsToAgency(String driverUuid, Long agencyId);

    /**
     * Retourne le nombre total de livreurs d'une agence
     */
    long countByAgency(Long agencyId);

    /**
     * Retourne le nombre de livreurs disponibles d'une agence
     */
    long countAvailableByAgency(Long agencyId);
}