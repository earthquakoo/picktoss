package com.picktoss.picktossserver.domain.document.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetMostIncorrectDocumentsResponse {

    private List<GetMostIncorrectDocumentsDto> documents;

    @Getter
    @Builder
    public static class GetMostIncorrectDocumentsDto {
        private Long id;
        private String name;
        private int incorrectAnswerCount;
        private GetMostIncorrectDocumentsCategoryDto category;
    }

    @Getter
    @Builder
    public static class GetMostIncorrectDocumentsCategoryDto {
        private Long id;
        private String name;
    }
}
