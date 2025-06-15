package com.picktoss.picktossserver.domain.document.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetIsNotPublicDocumentsResponse {

    private List<GetIsNotPublicDocuments> documents;

    @Getter
    @Builder
    public static class GetIsNotPublicDocuments {
        private Long id;
        private String name;
        private String emoji;
        private String previewContent;
        private Boolean isPublic;
        private int totalQuizCount;
    }
}
