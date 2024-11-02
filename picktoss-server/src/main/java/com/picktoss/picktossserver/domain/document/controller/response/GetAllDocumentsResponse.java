package com.picktoss.picktossserver.domain.document.controller.response;

import com.picktoss.picktossserver.global.enums.category.CategoryTag;
import com.picktoss.picktossserver.global.enums.document.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetAllDocumentsResponse {

    private List<GetAllDocumentsDocumentDto> documents;

    @Getter
    @Builder
    public static class GetAllDocumentsDocumentDto {
        private Long id;
        private String name;
        private int characterCount;
        private DocumentStatus status;
        private int totalQuizCount;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Integer reviewNeededQuizCount;
        private GetAllDocumentsCategoryDto category;
    }

    @Getter
    @Builder
    public static class GetAllDocumentsCategoryDto {
        private String name;
        private CategoryTag categoryTag;
    }
}
