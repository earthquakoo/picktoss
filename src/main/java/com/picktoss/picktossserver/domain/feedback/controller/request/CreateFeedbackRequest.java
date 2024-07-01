package com.picktoss.picktossserver.domain.feedback.controller.request;

import com.picktoss.picktossserver.global.enums.FeedbackType;
import lombok.Getter;

@Getter
public class CreateFeedbackRequest {
    private String content;
    private FeedbackType type;
}
