package com.delivery.delivery_api.tracking.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LocationUpdateRequest {

    @NotNull(message = "La latitude est obligatoire")
    private Double lat;

    @NotNull(message = "La longitude est obligatoire")
    private Double lng;

    // Null si le livreur envoie sa position hors mission
    private String orderUuid;
}