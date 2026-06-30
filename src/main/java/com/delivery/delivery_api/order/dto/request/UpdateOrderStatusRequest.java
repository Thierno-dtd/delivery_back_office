package com.delivery.delivery_api.order.dto.request;

import com.delivery.delivery_api.order.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateOrderStatusRequest {

    @NotNull(message = "Le statut est obligatoire")
    private OrderStatus status;

    private String note;

    private String cancellationReason;
}