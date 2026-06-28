package com.delivery.delivery_api.admin.service;

import com.delivery.delivery_api.admin.dto.request.CreateAdminRequest;
import com.delivery.delivery_api.admin.dto.request.UpdateAdminRequest;
import com.delivery.delivery_api.admin.dto.response.AdminResponse;
import com.delivery.delivery_api.admin.entity.Admin;
import com.delivery.delivery_api.admin.enums.AdminRole;
import com.delivery.delivery_api.shared.response.PageResponse;

import java.util.List;

public interface IAdminService {

    /**
     * Crée un nouvel admin (SUPER_ADMIN ou MANAGER) sans mot de passe —
     * un mot de passe temporaire est généré et envoyé par email
     */
    AdminResponse create(CreateAdminRequest request);

    /**
     * Retourne les détails d'un admin par son uuid
     */
    AdminResponse findByUuid(String uuid);

    /**
     * Retourne les détails d'un admin par l'email de son compte utilisateur
     */
    AdminResponse findByUserEmail(String email);

    /**
     * Retourne la liste paginée des admins filtrée par rôle
     */
    PageResponse<AdminResponse> findByRole(AdminRole role, int page, int size);

    /**
     * Retourne tous les admins rattachés à une agence donnée
     */
    List<AdminResponse> findByAgencyId(Long agencyId);

    /**
     * Retourne l'entité Admin de l'utilisateur connecté — utilisé par d'autres modules
     */
    Admin getAuthenticatedAdmin(String email);

    /**
     * Met à jour les informations d'un admin (nom, prénom, agence)
     */
    AdminResponse update(String uuid, UpdateAdminRequest request);

    /**
     * Réinitialise le mot de passe d'un admin et envoie le nouveau par email
     */
    void resetPassword(String uuid);

    /**
     * Désactive le compte d'un admin sans le supprimer
     */
    void deactivate(String uuid);
}