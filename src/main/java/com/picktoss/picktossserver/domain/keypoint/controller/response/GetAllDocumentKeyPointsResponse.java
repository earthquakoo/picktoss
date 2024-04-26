package com.picktoss.picktossserver.domain.keypoint.controller.response;

import com.picktoss.picktossserver.global.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetAllDocumentKeyPointsResponse {

    private List<GetAllDocumentDto> documents;

    @Getter
    @Builder
    public static class GetAllDocumentDto {
        private Long id;
        private String documentName;
        private DocumentStatus status;
        private String summary;
        private LocalDateTime createdAt;
        private List<GetAllKeyPointDto> keyPoints;
    }

    @Getter
    @Builder
    public static class GetAllKeyPointDto {
        private Long id;
        private String question;
        private String answer;
    }
}
