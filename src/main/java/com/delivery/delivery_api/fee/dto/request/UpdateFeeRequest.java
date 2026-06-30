package com.delivery.delivery_api.fee.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateFeeRequest {

    @DecimalMin(value = "0.0", inclusive = false,
            message = "Le seuil doit être supérieur à 0")
    private Double thresholdAmount;

    @DecimalMin(value = "0.0", inclusive = false,
            message = "Le montant fixe doit être supérieur à 0")
    private Double flatFee;

    @DecimalMin(value = "0.0", inclusive = false,
            message = "Le taux de commission doit être supérieur à 0")
    private Double commissionRate;

    @DecimalMin(value = "0.0", inclusive = false,
            message = "La commission minimale doit être supérieure à 0")
    private Double minFee;

    // null = ne pas modifier le plafond existant
    private Double maxFee;
}