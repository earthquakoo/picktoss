package com.picktoss.picktossserver.domain.document.controller.request;

import lombok.Getter;

@Getter
public class MoveDocumentToCategoryRequest {
    private Long documentId;
    private Long categoryId;
}
