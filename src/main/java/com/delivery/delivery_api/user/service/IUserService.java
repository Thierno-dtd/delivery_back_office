package com.delivery.delivery_api.user.service;

import com.delivery.delivery_api.user.dto.request.UpdateProfileRequest;
import com.delivery.delivery_api.user.dto.response.UserResponse;
import com.delivery.delivery_api.user.entity.User;

public interface IUserService {

    /**
     * Retourne le profil de l'utilisateur actuellement connecté
     */
    UserResponse getCurrentUser();

    /**
     * Met à jour le profil (email, mot de passe) de l'utilisateur connecté
     */
    UserResponse updateProfile(UpdateProfileRequest request);

    /**
     * Active ou désactive un compte utilisateur — réservé aux admins
     */
    void toggleActiveStatus(String uuid, boolean active);

    /**
     * Retourne l'entité User de l'utilisateur connecté depuis le SecurityContext
     */
    User getAuthenticatedUser();

    /**
     * Récupère un utilisateur par son uuid ou lève une exception si non trouvé
     */
    User findByUuidOrThrow(String uuid);

    /**
     * Récupère un utilisateur par son email ou lève une exception si non trouvé
     */
    User findByEmailOrThrow(String email);
}