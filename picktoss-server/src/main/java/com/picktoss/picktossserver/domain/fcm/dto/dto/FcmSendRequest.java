package com.picktoss.picktossserver.domain.fcm.dto.dto;

import lombok.Getter;

@Getter
public class FcmSendRequest {
    private String token;
    private String title;
    private String body;
}
