package com.delivery.delivery_api.driver.dto.response;

import com.delivery.delivery_api.driver.enums.DriverStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DriverResponse {

    private String uuid;
    private String firstName;
    private String lastName;
    private String email;
    private String telephone;
    private boolean phoneVerified;
    private String agencyUuid;
    private String agencyName;
    private LocalDate birthday;
    private String picture;
    private String address;
    private DriverStatus status;
    private boolean profileComplete;
    private LocalDateTime createdAt;
}