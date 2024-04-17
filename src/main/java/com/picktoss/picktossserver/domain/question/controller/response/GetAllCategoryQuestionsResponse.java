package com.picktoss.picktossserver.domain.question.controller.response;

import com.picktoss.picktossserver.global.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetAllCategoryQuestionsResponse {

    private List<DocumentDto> documents;

    @Getter
    @Builder
    public static class DocumentDto {
        private Long id;
        private String documentName;
        private DocumentStatus status;
        private String summary;
        private LocalDateTime createdAt;
        private List<QuestionDto> questions;
    }

    @Getter
    @Builder
    public static class QuestionDto {
        private Long id;
        private String question;
        private String answer;
    }
}
