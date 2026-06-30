package com.delivery.delivery_api.order.service;

import com.delivery.delivery_api.order.dto.request.AssignDriverRequest;
import com.delivery.delivery_api.order.dto.request.CreateOrderRequest;
import com.delivery.delivery_api.order.dto.request.UpdateOrderStatusRequest;
import com.delivery.delivery_api.order.dto.response.OrderResponse;
import com.delivery.delivery_api.order.dto.response.OrderStatusHistoryResponse;
import com.delivery.delivery_api.shared.response.PageResponse;

import java.util.List;

public interface IOrderService {

    /**
     * Crée une nouvelle commande de livraison — appelé par le client depuis l'app mobile.
     * Le profil client doit être complet avant de pouvoir commander.
     */
    OrderResponse create(CreateOrderRequest request);

    /**
     * Retourne les détails complets d'une commande par son uuid
     */
    OrderResponse findByUuid(String uuid);

    /**
     * Retourne les commandes du client connecté avec pagination
     */
    PageResponse<OrderResponse> getMyOrders(int page, int size);

    /**
     * Retourne les commandes d'un client donné — usage admin/manager
     */
    PageResponse<OrderResponse> getCustomerOrders(String customerUuid, int page, int size);

    /**
     * Retourne les commandes actives du livreur connecté
     */
    PageResponse<OrderResponse> getMyDriverOrders(int page, int size);

    /**
     * Retourne toutes les commandes d'une agence avec pagination —
     * usage manager/gestionnaire
     */
    PageResponse<OrderResponse> getAgencyOrders(Long agencyId, int page, int size);

    /**
     * Retourne les commandes en attente d'une agence — pour l'écran d'assignation
     */
    PageResponse<OrderResponse> getPendingOrdersByAgency(Long agencyId, int page, int size);

    /**
     * Assigne un livreur disponible à une commande PENDING —
     * réservé au manager/gestionnaire de l'agence
     */
    OrderResponse assignDriver(String orderUuid, AssignDriverRequest request);

    /**
     * Met à jour le statut d'une commande —
     * le livreur met à jour depuis l'app mobile (PICKUP, IN_TRANSIT, DELIVERED)
     * le manager/gestionnaire peut aussi annuler (CANCELLED)
     */
    OrderResponse updateStatus(String orderUuid, UpdateOrderStatusRequest request);

    /**
     * Annule une commande — client ou manager/gestionnaire
     * Seulement possible si statut PENDING ou ACCEPTED
     */
    OrderResponse cancel(String orderUuid, String reason);

    /**
     * Retourne l'historique complet des statuts d'une commande
     */
    List<OrderStatusHistoryResponse> getStatusHistory(String orderUuid);
}