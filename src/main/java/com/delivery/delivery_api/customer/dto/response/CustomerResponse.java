package com.delivery.delivery_api.customer.dto.response;

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
public class CustomerResponse {

    private String uuid;
    private String firstName;
    private String lastName;
    private String email;
    private String telephone;
    private boolean phoneVerified;
    private LocalDate birthday;
    private String gender;
    private String address;
    private boolean profileComplete;
    private LocalDateTime createdAt;
}