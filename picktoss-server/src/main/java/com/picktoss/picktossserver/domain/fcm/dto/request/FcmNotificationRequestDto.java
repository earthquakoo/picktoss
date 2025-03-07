package com.picktoss.picktossserver.domain.fcm.dto.request;


import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FcmNotificationRequestDto {

    private String title;
    private String body;
    private String content;
}
