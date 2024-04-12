package com.picktoss.picktossserver.domain.document.controller.response;

import com.picktoss.picktossserver.global.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetAllDocumentsResponse {

    private List<DocumentDto> documents;

    @Getter
    @Builder
    public static class DocumentDto {
        private Long id;
        private String documentName;
        private DocumentStatus status;
        private String summary;
        private LocalDateTime createdAt;
    }
}
