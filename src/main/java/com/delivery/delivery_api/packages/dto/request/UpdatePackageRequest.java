package com.delivery.delivery_api.packages.dto.request;

import com.delivery.delivery_api.packages.enums.PackageType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdatePackageRequest {

    private String description;
    private PackageType type;
    private Double declaredValue;
    private String photoUrl;
    private Double weight;
    private Double width;
    private Double height;
    private Double length;
    private Boolean insured;
    private Double insuranceAmount;
}