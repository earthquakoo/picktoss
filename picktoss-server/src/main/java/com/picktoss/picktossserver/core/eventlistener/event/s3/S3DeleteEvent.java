package com.picktoss.picktossserver.core.eventlistener.event.s3;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class S3DeleteEvent {
    private String s3Key;
}
