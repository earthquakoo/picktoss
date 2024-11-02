package com.picktoss.picktossserver.domain.category.controller.response;

import com.picktoss.picktossserver.global.enums.category.CategoryTag;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetSingleCategoryResponse {
    private Long id;
    private String name;
    private String emoji;
    private CategoryTag tag;
}
