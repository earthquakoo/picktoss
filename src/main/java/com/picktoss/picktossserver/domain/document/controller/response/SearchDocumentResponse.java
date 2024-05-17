package com.picktoss.picktossserver.domain.document.controller.response;

import com.picktoss.picktossserver.global.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class SearchDocumentResponse {

    private List<SearchDocumentDto> documents;

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
}
