package com.delivery.delivery_api.zone.dto.response;

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
public class ZoneResponse {

    private String uuid;
    private String agencyUuid;
    private String agencyName;
    private String name;
    private String city;
    private Double baseFee;
    private boolean active;
    private LocalDateTime createdAt;
}