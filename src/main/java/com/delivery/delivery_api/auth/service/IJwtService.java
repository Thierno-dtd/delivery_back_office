package com.delivery.delivery_api.auth.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.Map;

public interface IJwtService {

    /**
     * Génère un access token JWT pour l'utilisateur donné
     */
    String generateAccessToken(UserDetails userDetails);

    /**
     * Génère un access token JWT avec des claims supplémentaires (role, uuid...)
     */
    String generateAccessToken(Map<String, Object> extraClaims, UserDetails userDetails);

    /**
     * Génère un refresh token JWT de longue durée pour l'utilisateur donné
     */
    String generateRefreshToken(UserDetails userDetails);

    /**
     * Extrait l'email (subject) depuis un token JWT
     */
    String extractEmail(String token);

    /**
     * Extrait la date d'expiration d'un token JWT
     */
    Date extractExpiration(String token);

    /**
     * Extrait une claim spécifique par sa clé depuis un token JWT
     */
    Object extractClaim(String token, String claimKey);

    /**
     * Vérifie si un token JWT est valide pour l'utilisateur donné
     */
    boolean isTokenValid(String token, UserDetails userDetails);

    /**
     * Vérifie si un token JWT est expiré
     */
    boolean isTokenExpired(String token);
}