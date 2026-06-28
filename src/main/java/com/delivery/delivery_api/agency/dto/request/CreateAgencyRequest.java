package com.delivery.delivery_api.agency.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateAgencyRequest {

    @NotBlank(message = "Le nom de l'agence est obligatoire")
    private String name;

    private String slogan;

    @NotBlank(message = "L'adresse est obligatoire")
    private String address;

    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(
            regexp = "^(\\+241|0)[0-9]{8}$",
            message = "Numéro gabonais invalide. Format : +24177000000"
    )
    private String telephone;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    private String email;

    private String logoUrl;
}