package com.delivery.delivery_api.fee.service;

import com.delivery.delivery_api.fee.dto.request.CreateFeeRequest;
import com.delivery.delivery_api.fee.dto.request.UpdateFeeRequest;
import com.delivery.delivery_api.fee.dto.response.FeeResponse;

public interface IFeeService {

    /**
     * Crée la configuration de frais pour une agence —
     * une agence ne peut avoir qu'une seule configuration active
     */
    FeeResponse create(CreateFeeRequest request);

    /**
     * Retourne la configuration de frais d'une agence par l'uuid de la fee
     */
    FeeResponse findByUuid(String uuid);

    /**
     * Retourne la configuration de frais d'une agence par l'uuid de l'agence
     */
    FeeResponse findByAgencyUuid(String agencyUuid);

    /**
     * Met à jour la configuration de frais d'une agence
     */
    FeeResponse update(String uuid, UpdateFeeRequest request);

    /**
     * Active ou désactive la configuration de frais d'une agence
     */
    void toggleStatus(String uuid, boolean active);

    /**
     * Calcule la commission à prélever sur un montant de course donné —
     * applique la règle flatFee ou commissionRate selon le seuil
     * Utilisé par le module payment au moment du règlement
     */
    Double calculateCommission(Long agencyId, Double orderAmount);
}