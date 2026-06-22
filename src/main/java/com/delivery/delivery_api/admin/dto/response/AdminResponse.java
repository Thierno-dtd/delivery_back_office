package com.delivery.delivery_api.admin.dto.response;

import com.delivery.delivery_api.admin.enums.AdminRole;
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
public class AdminResponse {

    private String uuid;
    private String firstName;
    private String lastName;
    private String email;
    private AdminRole role;
    private Long agencyId;
    private boolean active;
    private LocalDateTime createdAt;
}