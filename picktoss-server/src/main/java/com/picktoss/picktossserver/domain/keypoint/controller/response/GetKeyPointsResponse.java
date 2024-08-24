package com.picktoss.picktossserver.domain.keypoint.controller.response;

import com.picktoss.picktossserver.domain.keypoint.controller.dto.KeyPointResponseDto;
import com.picktoss.picktossserver.global.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class GetKeyPointsResponse {

    private DocumentStatus documentStatus;
    private List<GetKeyPointsDto> keyPoints;

    @Getter
    @Builder
    public static class GetKeyPointsDto {
        private Long id;
        private String question;
        private String answer;
        private boolean bookmark;
        private LocalDateTime updatedAt;
    }
}
