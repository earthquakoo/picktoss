package com.picktoss.picktossserver.domain.document.controller.response;

import com.picktoss.picktossserver.global.enums.directory.DirectoryTag;
import com.picktoss.picktossserver.global.enums.document.DocumentStatus;
import com.picktoss.picktossserver.global.enums.document.DocumentType;
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
        private String previewContent;
        private int characterCount;
        private DocumentStatus status;
        private int totalQuizCount;
        private DocumentType documentType;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private Integer reviewNeededQuizCount;
        private GetAllDocumentsDirectoryDto directory;
    }

    @Getter
    @Builder
    public static class GetAllDocumentsDirectoryDto {
        private String name;
        private DirectoryTag tag;
    }
}
