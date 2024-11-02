package com.picktoss.picktossserver.domain.document.controller.request;

import lombok.Getter;

import java.util.Map;

@Getter
public class UpdateTodayQuizSettingsRequest {
    private Map<Long, Boolean> documentIdTodayQuizMap;
}
