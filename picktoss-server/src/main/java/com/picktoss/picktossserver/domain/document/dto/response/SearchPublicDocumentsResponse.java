package com.picktoss.picktossserver.domain.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SearchPublicDocumentsResponse {

    private List<SearchPublicDocumentsDto> publicDocuments;

    @Getter
    @Builder
    public static class SearchPublicDocumentsDto {
        private Long id;
        private String name;
        private String emoji;
        private String category;
        private String creatorName;
        private Boolean isBookmarked;
        private int tryCount;
        private int bookmarkCount;
        private int totalQuizCount;
    }
}
