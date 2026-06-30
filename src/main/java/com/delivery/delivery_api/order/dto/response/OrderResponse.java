package com.delivery.delivery_api.order.dto.response;

import com.delivery.delivery_api.order.enums.OrderStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {

    private String uuid;
    private String orderCode;
    private String customerUuid;
    private String customerName;
    private String driverUuid;
    private String driverName;
    private String driverTelephone;
    private String agencyUuid;
    private String agencyName;
    private String zoneUuid;
    private String zoneName;
    private String departureAddress;
    private Double departureLat;
    private Double departureLng;
    private String arrivalAddress;
    private Double arrivalLat;
    private Double arrivalLng;
    private Double estimatedPrice;
    private Double finalPrice;
    private Double commissionAmount;
    private OrderStatus status;
    private String note;
    private String cancellationReason;
    private LocalDateTime deliveredAt;
    private List<OrderStatusHistoryResponse> statusHistory;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}