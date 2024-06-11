package com.picktoss.picktossserver.domain.category.controller.response;

import com.picktoss.picktossserver.global.enums.CategoryTag;
import com.picktoss.picktossserver.global.enums.DocumentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetAllCategoriesResponse {

    private List<GetAllCategoriesCategoryDto> categories;
    @Getter
    @Builder
    public static class GetAllCategoriesCategoryDto {
        private Long id;
        private String name;
        private CategoryTag tag;
        private int order;
        private String emoji;
        private List<GetAllCategoriesDocumentDto> documents;
    }

    @Getter
    @Builder
    public static class GetAllCategoriesDocumentDto {
        private Long id;
        private String name;
        private int order;
        private DocumentStatus documentStatus;
    }
}
