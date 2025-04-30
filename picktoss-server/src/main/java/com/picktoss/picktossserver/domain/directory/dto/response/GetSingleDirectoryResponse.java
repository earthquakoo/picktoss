package com.picktoss.picktossserver.domain.directory.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GetSingleDirectoryResponse {
    private Long id;
    private String name;
    private String emoji;
    private List<GetSingleDirectoryDocumentDto> documents;
    private List<GetSingleDirectoryBookmarkDto> bookmarks;

    @Getter
    @Builder
    public static class GetSingleDirectoryDocumentDto {
        private Long id;
        private String name;
        private String previewContent;
        private String emoji;
        private int totalQuizCount;
    }

    @Getter
    @Builder
    public static class GetSingleDirectoryBookmarkDto {
        private Long id;
        private String name;
        private String emoji;
        private String previewContent;
        private int totalQuizCount;
        private int tryCount;
        private int bookmarkCount;
    }
}
