package com.delivery.delivery_api.customer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CompleteProfileRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(
            regexp = "^(\\+241|0)[0-9]{8}$",
            message = "Numéro gabonais invalide. Format : +24177000000"
    )
    private String telephone;

    private LocalDate birthday;

    private String gender;

    private String address;
}