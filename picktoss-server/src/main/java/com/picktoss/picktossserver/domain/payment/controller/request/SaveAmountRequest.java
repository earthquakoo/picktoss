package com.picktoss.picktossserver.domain.payment.controller.request;

import lombok.Getter;

@Getter
public class SaveAmountRequest {

    private String orderId;
    private int amount;
}
