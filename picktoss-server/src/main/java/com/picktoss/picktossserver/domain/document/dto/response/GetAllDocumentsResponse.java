package com.picktoss.picktossserver.domain.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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
        private String emoji;
        private String previewContent;
        private int tryCount;
        private int bookmarkCount;
        private int totalQuizCount;
        private Boolean isPublic;
        private Integer reviewNeededQuizCount;
    }
}
