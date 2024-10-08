package com.picktoss.picktossserver.domain.document.controller.request;

import lombok.Getter;

import java.util.List;

@Getter
public class UpdateDocumentToNotGenerateByTodayQuizRequest {
    private List<Long> documentIds;
}
