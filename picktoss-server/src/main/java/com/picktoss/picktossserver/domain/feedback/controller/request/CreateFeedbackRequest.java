package com.picktoss.picktossserver.domain.feedback.controller.request;

import com.picktoss.picktossserver.global.enums.feedback.FeedbackType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@AllArgsConstructor
public class CreateFeedbackRequest {
    private MultipartFile file;
    private String title;
    private String content;
    private FeedbackType type;
    private String email;
}
