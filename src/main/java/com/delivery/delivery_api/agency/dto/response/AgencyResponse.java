package com.delivery.delivery_api.agency.dto.response;

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
public class AgencyResponse {

    private String uuid;
    private String name;
    private String slogan;
    private String address;
    private String telephone;
    private String email;
    private String logoUrl;
    private boolean active;
    private long totalDrivers;       // enrichi par le service
    private long activeOrders;       // enrichi plus tard (module order)
    private LocalDateTime createdAt;
}