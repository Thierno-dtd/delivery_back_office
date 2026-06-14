package com.delivery.delivery_api.auth.controller;

import com.delivery.delivery_api.auth.dto.request.LoginRequest;
import com.delivery.delivery_api.auth.dto.request.RefreshTokenRequest;
import com.delivery.delivery_api.auth.dto.request.RegisterRequest;
import com.delivery.delivery_api.auth.dto.response.AuthResponse;
import com.delivery.delivery_api.auth.service.AuthService;
import com.delivery.delivery_api.shared.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentification", description = "Login, Register, Refresh token, Logout")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Retourne un access token et un refresh token")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.login(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Connexion réussie", response));
    }

    @PostMapping("/register")
    @Operation(summary = "Inscription client", description = "Crée un nouveau compte client")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest) {

        AuthResponse response = authService.register(request, httpRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Compte créé avec succès", response));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir le token", description = "Génère un nouvel access token")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token rafraîchi", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Déconnexion", description = "Révoque le token courant")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Authorization") String authHeader,
            HttpServletRequest httpRequest) {

        authService.logout(authHeader, httpRequest);
        return ResponseEntity.ok(ApiResponse.success("Déconnexion réussie"));
    }
}