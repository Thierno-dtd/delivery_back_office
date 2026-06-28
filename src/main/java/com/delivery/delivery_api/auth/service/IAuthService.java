package com.delivery.delivery_api.auth.service;

import com.delivery.delivery_api.auth.dto.request.LoginRequest;
import com.delivery.delivery_api.auth.dto.request.RefreshTokenRequest;
import com.delivery.delivery_api.auth.dto.request.RegisterRequest;
import com.delivery.delivery_api.auth.dto.response.AuthResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface IAuthService {

    /**
     * Authentifie un utilisateur et retourne les tokens JWT (access + refresh)
     */
    AuthResponse login(LoginRequest request, HttpServletRequest httpRequest);

    /**
     * Crée un nouveau compte client et retourne les tokens JWT
     */
    AuthResponse register(RegisterRequest request, HttpServletRequest httpRequest);

    /**
     * Génère un nouvel access token à partir d'un refresh token valide
     */
    AuthResponse refreshToken(RefreshTokenRequest request);

    /**
     * Révoque le token courant et supprime le refresh token de Redis
     */
    void logout(String authHeader, HttpServletRequest httpRequest);
}