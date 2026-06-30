package com.delivery.delivery_api.fee.dto.response;

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
public class FeeResponse {

    private String uuid;
    private String agencyUuid;
    private String agencyName;
    private Double thresholdAmount;
    private Double flatFee;
    private Double commissionRate;
    private Double minFee;
    private Double maxFee;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}