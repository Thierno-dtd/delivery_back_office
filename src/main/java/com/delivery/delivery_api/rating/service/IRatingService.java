package com.delivery.delivery_api.rating.service;

import com.delivery.delivery_api.rating.dto.request.CreateRatingRequest;
import com.delivery.delivery_api.rating.dto.response.RatingResponse;
import com.delivery.delivery_api.shared.response.PageResponse;

import java.util.List;

public interface IRatingService {

    /**
     * Crée une notation après livraison —
     * le customer note le driver et inversement.
     * Seulement possible si la commande est en statut DELIVERED.
     * Un utilisateur ne peut noter qu'une seule fois par commande.
     */
    RatingResponse create(CreateRatingRequest request);

    /**
     * Retourne toutes les notes d'une commande donnée
     */
    List<RatingResponse> findByOrder(String orderUuid);

    /**
     * Retourne les notes reçues par un utilisateur avec pagination
     */
    PageResponse<RatingResponse> findByRated(String userUuid, int page, int size);

    /**
     * Retourne la note moyenne d'un utilisateur —
     * utilisée pour afficher la réputation du livreur
     */
    Double getAverageScore(String userUuid);

    /**
     * Retourne le nombre total de notes reçues par un utilisateur
     */
    long getTotalRatings(String userUuid);
}