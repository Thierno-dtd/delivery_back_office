package com.delivery.delivery_api.agency.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateAgencyRequest {

    private String name;
    private String slogan;
    private String address;

    @Pattern(
            regexp = "^(\\+241|0)[0-9]{8}$",
            message = "Numéro gabonais invalide. Format : +24177000000"
    )
    private String telephone;

    @Email(message = "Format email invalide")
    private String email;

    private String logoUrl;
}