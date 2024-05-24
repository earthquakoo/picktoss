package com.picktoss.picktossserver.domain.document.controller.response;

import com.picktoss.picktossserver.global.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetSingleDocumentResponse {

    private Long id;
    private String documentName;
    private DocumentStatus status;
    private boolean quizGenerationStatus;
    private GetSingleDocumentCategoryDto category;
    private List<GetSingleDocumentKeyPointDto> keyPoints;
    private String content;
    private LocalDateTime createdAt;

    @Getter
    @Builder
    public static class GetSingleDocumentCategoryDto {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    public static class GetSingleDocumentKeyPointDto {
        private Long id;
        private String question;
        private String answer;
        private boolean bookmark;
    }
}
