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

    private List<GetAllDocumentsDocumentDto> documents;

    @Getter
    @Builder
    public static class GetAllDocumentsDocumentDto {
        private Long id;
        private String name;
        private DocumentStatus status;
        private boolean quizGenerationStatus;
        private LocalDateTime createdAt;
    }
}
