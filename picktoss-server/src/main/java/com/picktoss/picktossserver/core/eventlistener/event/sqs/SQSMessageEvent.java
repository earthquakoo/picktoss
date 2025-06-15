package com.picktoss.picktossserver.core.eventlistener.event.sqs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SQSMessageEvent {

    private Long memberId;
    private String s3Key;
    private Long documentId;
    private Integer starCount;
}
