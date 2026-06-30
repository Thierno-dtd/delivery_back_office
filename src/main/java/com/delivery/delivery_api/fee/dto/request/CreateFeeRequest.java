package com.delivery.delivery_api.fee.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateFeeRequest {

    @NotNull(message = "L'agence est obligatoire")
    private Long agencyId;

    @NotNull(message = "Le seuil de décision est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false,
            message = "Le seuil doit être supérieur à 0")
    private Double thresholdAmount;

    @NotNull(message = "Le montant fixe est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false,
            message = "Le montant fixe doit être supérieur à 0")
    private Double flatFee;

    @NotNull(message = "Le taux de commission est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false,
            message = "Le taux de commission doit être supérieur à 0")
    private Double commissionRate;

    @NotNull(message = "La commission minimale est obligatoire")
    @DecimalMin(value = "0.0", inclusive = false,
            message = "La commission minimale doit être supérieure à 0")
    private Double minFee;

    // Optionnel — null = pas de plafond
    private Double maxFee;
}