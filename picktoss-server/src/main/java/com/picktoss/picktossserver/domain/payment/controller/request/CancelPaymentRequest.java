package com.picktoss.picktossserver.domain.payment.controller.request;

import lombok.Getter;

@Getter
public class CancelPaymentRequest {
    private String paymentKey;
}
