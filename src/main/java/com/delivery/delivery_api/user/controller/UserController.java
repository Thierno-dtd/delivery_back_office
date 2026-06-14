package com.delivery.delivery_api.user.controller;

import com.delivery.delivery_api.shared.response.ApiResponse;
import com.delivery.delivery_api.user.dto.request.UpdateProfileRequest;
import com.delivery.delivery_api.user.dto.response.UserResponse;
import com.delivery.delivery_api.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/users")
@RequiredArgsConstructor
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Utilisateurs", description = "Profil de l'utilisateur connecté")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Mon profil", description = "Retourne les infos de l'utilisateur connecté")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        UserResponse response = userService.getCurrentUser();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/me")
    @Operation(summary = "Modifier mon profil", description = "Mise à jour email ou mot de passe")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse response = userService.updateProfile(request);
        return ResponseEntity.ok(ApiResponse.success("Profil mis à jour", response));
    }
}
