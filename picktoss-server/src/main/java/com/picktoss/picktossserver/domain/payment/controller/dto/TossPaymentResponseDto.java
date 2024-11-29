package com.picktoss.picktossserver.domain.payment.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class TossPaymentResponseDto {

    @JsonProperty("paymentKey")
    private String paymentKey;

    @JsonProperty("orderId")
    private String orderId;

    @JsonProperty("method")
    private String paymentMethod;

    @JsonProperty("status")
    private String paymentStatus;

    @JsonProperty("totalAmount")
    private Integer amount;

    @JsonProperty("requestedAt")
    private OffsetDateTime requestAt;

    @JsonProperty("approvedAt")
    private OffsetDateTime approveAt;
}
