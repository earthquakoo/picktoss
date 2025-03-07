package com.picktoss.picktossserver.domain.fcm.dto.dto;

import lombok.*;

@Getter
@ToString
@Builder
public class FcmSendDto {
    private String token;
    private String title;
    private String body;
}
