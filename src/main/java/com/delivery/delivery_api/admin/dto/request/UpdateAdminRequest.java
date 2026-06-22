package com.delivery.delivery_api.admin.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateAdminRequest {

    private String firstName;
    private String lastName;
    private Long agencyId;
}