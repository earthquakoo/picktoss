package com.picktoss.picktossserver.domain.document.controller.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class ChangeDocumentsOrderRequest {

    private List<ChangeDocumentDto> documents;

    @Getter
    public static class ChangeDocumentDto {
        private Long id;
        private int order;
    }
}
