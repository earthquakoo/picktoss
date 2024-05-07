package com.picktoss.picktossserver.domain.document.controller.response;

import com.picktoss.picktossserver.global.enums.DocumentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SearchDocumentNameResponse {

    private Long id;
    private String name;
    private DocumentStatus status;
    private boolean quizGenerationStatus;
    private LocalDateTime createdAt;

}
