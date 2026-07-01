package com.delivery.delivery_api.packages.dto.response;

import com.delivery.delivery_api.packages.enums.PackageType;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PackageResponse {

    private String uuid;
    private String orderUuid;
    private String orderCode;
    private String description;
    private PackageType type;
    private Double declaredValue;
    private String photoUrl;
    private Double weight;
    private Double width;
    private Double height;
    private Double length;
    private boolean insured;
    private Double insuranceAmount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}