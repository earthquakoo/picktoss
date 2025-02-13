package com.picktoss.picktossserver.domain.payment.dto.request;

import lombok.Getter;

@Getter
public class VerifyPaymentRequest {
    private String impUid;
    private int amount;
}
