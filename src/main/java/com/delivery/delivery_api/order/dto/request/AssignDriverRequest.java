package com.delivery.delivery_api.order.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AssignDriverRequest {

    @NotBlank(message = "L'uuid du livreur est obligatoire")
    private String driverUuid;
}