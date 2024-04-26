package com.picktoss.picktossserver.domain.document.controller.request;

import com.picktoss.picktossserver.domain.category.controller.request.UpdateCategoriesOrderRequest;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UpdateDocumentsOrderRequest {

    private List<UpdateDocumentDto> documents;

    @Getter
    public static class UpdateDocumentDto {
        private Long id;
        private int order;
    }
}
