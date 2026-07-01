package com.delivery.delivery_api.packages.service;

import com.delivery.delivery_api.packages.dto.request.CreatePackageRequest;
import com.delivery.delivery_api.packages.dto.request.UpdatePackageRequest;
import com.delivery.delivery_api.packages.dto.response.PackageResponse;

import java.util.List;

public interface IPackageService {

    /**
     * Ajoute un colis à une commande existante —
     * la commande doit être en statut PENDING pour permettre l'ajout
     */
    PackageResponse create(CreatePackageRequest request);

    /**
     * Retourne les détails d'un colis par son uuid
     */
    PackageResponse findByUuid(String uuid);

    /**
     * Retourne tous les colis d'une commande donnée
     */
    List<PackageResponse> findByOrder(String orderUuid);

    /**
     * Met à jour les informations d'un colis —
     * seulement possible si la commande est encore en statut PENDING
     */
    PackageResponse update(String uuid, UpdatePackageRequest request);

    /**
     * Supprime un colis d'une commande —
     * seulement possible si la commande est encore en statut PENDING
     */
    void delete(String uuid);

    /**
     * Retourne le nombre de colis pour une commande donnée
     */
    long countByOrder(String orderUuid);
}