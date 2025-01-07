package com.picktoss.picktossserver.domain.document.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class MoveDocumentToDirectoryRequest {
    private List<Long> documentIds;
    private Long directoryId;
}
