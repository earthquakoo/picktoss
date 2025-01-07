package com.picktoss.picktossserver.domain.document.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class DeleteDocumentRequest {
    private List<Long> documentIds;
}
