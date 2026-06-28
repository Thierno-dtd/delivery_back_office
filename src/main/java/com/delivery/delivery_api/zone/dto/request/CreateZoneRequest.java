package com.delivery.delivery_api.zone.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateZoneRequest {

    @NotNull(message = "L'agence est obligatoire")
    private Long agencyId;

    @NotBlank(message = "Le nom de la zone est obligatoire")
    private String name;

    @NotBlank(message = "La ville est obligatoire")
    private String city;

    @NotNull(message = "Le tarif de base est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le tarif doit être supérieur à 0")
    private Double baseFee;
}