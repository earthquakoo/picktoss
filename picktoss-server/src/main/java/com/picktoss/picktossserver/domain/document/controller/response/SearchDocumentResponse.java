package com.picktoss.picktossserver.domain.document.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SearchDocumentResponse {

    private List<SearchDocumentDto> documents;
    private List<SearchDocumentQuizDto> quizzes;

    @Getter
    @Builder
    public static class SearchDocumentDto {
        private Long documentId;
        private String documentName;
        private String content;
        private SearchDocumentCategoryDto category;
    }

    @Getter
    @Builder
    public static class SearchDocumentCategoryDto {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    public static class SearchDocumentQuizDto {
        private Long id;
        private String question;
        private String answer;
        private String documentName;
        private String categoryName;
    }
}
