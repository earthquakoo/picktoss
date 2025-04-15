package com.picktoss.picktossserver.domain.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetPublicDocumentsResponse {

    private int totalPages;
    private long totalDocuments;
    private List<GetPublicDocumentsDto> documents;

    @Getter
    @Builder
    public static class GetPublicDocumentsDto {
        private Long id;
        private String name;
        private String emoji;
        private String previewContent;
        private int tryCount;
        private int bookmarkCount;
        private int totalQuizCount;
        private Boolean isBookmarked;
    }
}
