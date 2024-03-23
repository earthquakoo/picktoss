package com.picktoss.picktossserver.domain.category.controller.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetAllCategoriesResponse {

    private List<CategoryDto> categories;

    @Getter
    @Builder
    public static class CategoryDto {
        private Long id;
        private String name;
    }
}
