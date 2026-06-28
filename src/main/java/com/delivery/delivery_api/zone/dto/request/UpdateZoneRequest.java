package com.delivery.delivery_api.zone.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateZoneRequest {

    private String name;
    private String city;

    @DecimalMin(value = "0.0", inclusive = false, message = "Le tarif doit être supérieur à 0")
    private Double baseFee;
}