package com.picktoss.picktossserver.domain.keypoint.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class KeyPointResponseDto {

    private List<KeyPointDto> keyPoints;

    @Getter
    @Builder
    public static class KeyPointDto {
        private Long id;
        private String question;
        private String answer;
        private CategoryDto category;
        private DocumentDto document;
        private LocalDateTime updatedAt;
    }

    @Getter
    @Builder
    public static class CategoryDto {
        private Long id;
        private String name;
    }

    @Getter
    @Builder
    public static class DocumentDto {
        private Long id;
        private String name;
    }
}
