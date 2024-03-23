package com.picktoss.picktossserver.domain.category.controller.mapper;


import com.picktoss.picktossserver.domain.category.controller.dto.CategoryResponseDto;
import com.picktoss.picktossserver.domain.category.entity.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoryMapper {

    public static List<CategoryResponseDto> mapCategoriesToCategoryDtos(List<Category> categories) {
        // Base case
        if (categories.isEmpty()) return new ArrayList<>();

        List<CategoryResponseDto> categoryDtos = new ArrayList<>();

        for (Category category : categories) {
            CategoryResponseDto categoryDto = CategoryResponseDto.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .build();

            categoryDtos.add(categoryDto);
        }

        return categoryDtos;
    }
}
