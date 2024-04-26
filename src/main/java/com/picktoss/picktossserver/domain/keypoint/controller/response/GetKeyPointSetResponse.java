package com.picktoss.picktossserver.domain.keypoint.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetKeyPointSetResponse {
    private List<GetKeyPointDto> questions;

    @Getter
    @Builder
    public static class GetKeyPointDto {
        private Long id;
        private String question;
        private String answer;
        private GetDocumentDto document;
        private GetCategoryDto category;
    }

    @Getter
    @Builder
    public static class GetDocumentDto {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    public static class GetCategoryDto {
        private Long id;
        private String name;
    }
}
