package com.picktoss.picktossserver.domain.document.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

import java.util.Map;

@Getter
public class UpdateTodayQuizSettingsRequest {
    @Schema(
            description = "문서 ID와 퀴즈 설정 값 (true/false) 맵",
            example = "{\"1\": true, \"2\": false, \"3\": true}"
    )
    private Map<Long, Boolean> documentIdTodayQuizMap;
}
