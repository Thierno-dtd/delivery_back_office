package com.delivery.delivery_api.customer.service;

import com.delivery.delivery_api.customer.dto.request.CompleteProfileRequest;
import com.delivery.delivery_api.customer.dto.request.UpdateCustomerRequest;
import com.delivery.delivery_api.customer.dto.response.CustomerResponse;
import com.delivery.delivery_api.customer.entity.Customer;
import com.delivery.delivery_api.shared.response.PageResponse;

public interface ICustomerService {

    /**
     * Complète le profil client après inscription (nom, prénom, téléphone)
     * Appelé une seule fois juste après le register côté mobile
     */
    CustomerResponse completeProfile(CompleteProfileRequest request);

    /**
     * Retourne le profil du client actuellement connecté
     */
    CustomerResponse getCurrentProfile();

    /**
     * Met à jour les informations du client connecté
     */
    CustomerResponse updateProfile(UpdateCustomerRequest request);

    /**
     * Retourne les détails d'un client par son uuid — usage admin/manager
     */
    CustomerResponse findByUuid(String uuid);

    /**
     * Recherche des clients par nom ou prénom — usage admin/manager
     */
    PageResponse<CustomerResponse> search(String query, int page, int size);

    /**
     * Marque le téléphone d'un client comme vérifié (après OTP)
     */
    void verifyPhone(String telephone);

    /**
     * Retourne l'entité Customer du client connecté — utilisé par le module order
     */
    Customer getAuthenticatedCustomer();

    /**
     * Retourne l'entité Customer par uuid ou lève une exception — utilisé par d'autres modules
     */
    Customer getByUuidOrThrow(String uuid);

    /**
     * Retourne le nombre total de clients inscrits — utilisé pour les statistiques
     */
    long countTotal();
}