package com.picktoss.picktossserver.domain.feedback.controller.request;

import com.picktoss.picktossserver.global.enums.feedback.FeedbackType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@AllArgsConstructor
public class CreateFeedbackRequest {
    private List<MultipartFile> files;
    private String title;
    private String content;
    private FeedbackType type;
    private String email;
}
