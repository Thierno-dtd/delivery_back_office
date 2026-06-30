package com.delivery.delivery_api.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateOrderRequest {

    @NotNull(message = "L'agence est obligatoire")
    private Long agencyId;

    private Long zoneId;

    @NotBlank(message = "L'adresse de départ est obligatoire")
    private String departureAddress;

    private Double departureLat;
    private Double departureLng;

    @NotBlank(message = "L'adresse d'arrivée est obligatoire")
    private String arrivalAddress;

    private Double arrivalLat;
    private Double arrivalLng;

    private String note;
}