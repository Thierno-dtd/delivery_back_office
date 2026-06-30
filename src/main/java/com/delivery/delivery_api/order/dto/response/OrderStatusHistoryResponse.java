package com.delivery.delivery_api.order.dto.response;

import com.delivery.delivery_api.order.enums.OrderStatus;
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
public class OrderStatusHistoryResponse {

    private OrderStatus status;
    private String changedBy;
    private String note;
    private LocalDateTime createdAt;
}