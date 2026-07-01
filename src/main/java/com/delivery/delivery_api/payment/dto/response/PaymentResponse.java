package com.delivery.delivery_api.payment.dto.response;

import com.delivery.delivery_api.payment.enums.PaymentMethod;
import com.delivery.delivery_api.payment.enums.PaymentStatus;
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
public class PaymentResponse {

    private String uuid;
    private String orderUuid;
    private String orderCode;
    private Double amount;
    private Double commissionAmount;
    private Double agencyAmount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionReference;
    private LocalDateTime transactionDate;
    private String failureReason;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}