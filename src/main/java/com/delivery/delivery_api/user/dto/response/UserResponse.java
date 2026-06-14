package com.delivery.delivery_api.user.dto.response;

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
public class UserResponse {

    private String uuid;
    private String email;
    private boolean emailVerified;
    private boolean active;
    private LocalDateTime lastLogin;
    private LocalDateTime createdAt;
}