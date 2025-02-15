package com.picktoss.picktossserver.domain.payment.dto.request;

import lombok.Getter;

@Getter
public class CreatePaymentsRequest {
    private String impUid;
    private int amount;
}
