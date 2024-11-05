package com.picktoss.picktossserver.domain.document.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetDocumentsNeedingReviewResponse {

    private List<GetReviewNeededDocumentsDto> documents;

    @Getter
    @Builder
    public static class GetReviewNeededDocumentsDto {
        private Long id;
        private String name;
        private Integer reviewNeededQuizCount;
        private GetReviewNeededDocumentsDirectoryDto directory;
    }

    @Getter
    @Builder
    public static class GetReviewNeededDocumentsDirectoryDto {
        private Long id;
        private String name;
    }
}
