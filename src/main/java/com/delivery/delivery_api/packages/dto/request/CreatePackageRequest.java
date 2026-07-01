package com.delivery.delivery_api.packages.dto.request;

import com.delivery.delivery_api.packages.enums.PackageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreatePackageRequest {

    @NotNull(message = "L'uuid de la commande est obligatoire")
    private String orderUuid;

    @NotBlank(message = "La description du colis est obligatoire")
    private String description;

    @NotNull(message = "Le type de colis est obligatoire")
    private PackageType type;

    private Double declaredValue;
    private String photoUrl;
    private Double weight;
    private Double width;
    private Double height;
    private Double length;
    private boolean insured = false;
    private Double insuranceAmount;
}