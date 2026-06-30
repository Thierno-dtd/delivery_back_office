package com.delivery.delivery_api.driver.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class UpdateDriverRequest {

    private String firstName;
    private String lastName;

    @Pattern(
            regexp = "^(\\+241|0)[0-9]{8}$",
            message = "Numéro gabonais invalide. Format : +24177000000"
    )
    private String telephone;

    private LocalDate birthday;
    private String address;
    private String picture;
    private String identityDoc;
}