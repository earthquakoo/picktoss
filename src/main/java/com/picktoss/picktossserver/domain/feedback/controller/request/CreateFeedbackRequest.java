package com.picktoss.picktossserver.domain.feedback.controller.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateFeedbackRequest {
    private String content;
}
