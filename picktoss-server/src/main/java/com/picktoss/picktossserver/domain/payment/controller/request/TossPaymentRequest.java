package com.picktoss.picktossserver.domain.payment.controller.request;

import lombok.Getter;

@Getter
public class TossPaymentRequest {

    private String paymentKey;
    private String orderId;
    private Integer amount;
}
