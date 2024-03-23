package com.picktoss.picktossserver.domain.document.controller.response;

import com.picktoss.picktossserver.global.enums.DocumentFormat;
import com.picktoss.picktossserver.global.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetSingleDocumentResponse {

    private List<DocumentDto> documents;

    @Getter
    @Builder
    public static class DocumentDto {
        private Long documentId;
        private String documentName;
        private DocumentStatus status;
        private DocumentFormat format;
        private CategoryDto categoryDto;
        private List<QuestionDto> questionDtos;
        private String summary;
        private String content;
    }

    @Getter
    @Builder
    public static class CategoryDto {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    public static class QuestionDto {
        private Long id;
        private String question;
        private String answer;
    }
}
