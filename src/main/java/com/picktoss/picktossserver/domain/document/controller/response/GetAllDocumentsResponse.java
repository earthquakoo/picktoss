package com.picktoss.picktossserver.domain.document.controller.response;

import com.picktoss.picktossserver.global.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetAllDocumentsResponse {

    private List<DocumentDto> documents;

    @Getter
    @Builder
    public static class DocumentDto {
        private Long documentId;
        private String documentName;
        private DocumentStatus status;
        private String summary;
    }
}
