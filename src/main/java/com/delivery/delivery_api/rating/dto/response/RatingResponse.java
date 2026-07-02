package com.delivery.delivery_api.rating.dto.response;

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
public class RatingResponse {

    private Long id;
    private String orderUuid;
    private String orderCode;
    private String raterUuid;
    private String raterName;
    private String ratedUuid;
    private String ratedName;
    private Integer score;
    private String comment;
    private LocalDateTime createdAt;
}