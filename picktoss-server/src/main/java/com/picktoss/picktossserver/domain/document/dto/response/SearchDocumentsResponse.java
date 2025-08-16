package com.picktoss.picktossserver.domain.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class SearchDocumentsResponse {

    private List<SearchDocumentsDto> documents;

    @Getter
    @Builder
    public static class SearchDocumentsDto {
        private Long id;
        private String name;
        private String emoji;
        private Boolean isOwner;
        private Boolean isPublic;
        private Boolean isBookmarked;
        private int tryCount;
        private int bookmarkCount;
        private int totalQuizCount;
        private List<SearchDocumentsQuizDto> quizzes;
    }

    @Getter
    @Builder
    public static class SearchDocumentsQuizDto {
        private String question;
        private String answer;
        private String explanation;
    }
}
