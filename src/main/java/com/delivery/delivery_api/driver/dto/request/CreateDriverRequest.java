package com.delivery.delivery_api.driver.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CreateDriverRequest {

    @NotBlank(message = "Le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String lastName;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format email invalide")
    private String email;

    @NotBlank(message = "Le numéro de téléphone est obligatoire")
    @Pattern(
            regexp = "^(\\+241|0)[0-9]{8}$",
            message = "Numéro gabonais invalide. Format : +24177000000"
    )
    private String telephone;

    @NotNull(message = "L'agence est obligatoire")
    private Long agencyId;

    private LocalDate birthday;
    private String address;
}