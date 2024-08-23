package com.picktoss.picktossserver.domain.document.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateDocumentsOrderRequest {

    private Long documentId;
    private int preDragDocumentOrder;
    private int afterDragDocumentOrder;
}
