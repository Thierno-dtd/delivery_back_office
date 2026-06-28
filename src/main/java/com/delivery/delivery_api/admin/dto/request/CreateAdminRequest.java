package com.delivery.delivery_api.admin.dto.request;

import com.delivery.delivery_api.admin.enums.AdminRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateAdminRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    private String email;


    @NotNull(message = "Le rôle est obligatoire")
    private AdminRole role;

    // Obligatoire seulement si role = MANAGER
    private Long agencyId;
}