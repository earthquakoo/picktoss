package com.picktoss.picktossserver.domain.category.controller.response;

import com.picktoss.picktossserver.global.enums.category.CategoryTag;
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
        private String emoji;
    }
}
