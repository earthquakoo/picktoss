package com.picktoss.picktossserver.domain.keypoint.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetBookmarkedKeyPointResponse {

    private List<GetBookmarkedKeyPointDto> keyPoints;


    @Getter
    @Builder
    public static class GetBookmarkedKeyPointDto {
        private String question;
        private String answer;
        private GetBookmarkedKeyPointDocumentDto document;
        private GetBookmarkedKeyPointCategoryDto category;
    }

    @Getter
    @Builder
    public static class GetBookmarkedKeyPointDocumentDto {
        private Long documentId;
        private String documentName;
    }

    @Getter
    @Builder
    public static class GetBookmarkedKeyPointCategoryDto {
        private Long categoryId;
        private String categoryName;
    }
}
