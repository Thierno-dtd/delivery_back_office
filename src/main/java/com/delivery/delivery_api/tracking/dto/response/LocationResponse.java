package com.delivery.delivery_api.tracking.dto.response;

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
public class LocationResponse {

    private String driverUuid;
    private String driverName;
    private Double lat;
    private Double lng;
    private String orderUuid;
    private LocalDateTime timestamp;
}