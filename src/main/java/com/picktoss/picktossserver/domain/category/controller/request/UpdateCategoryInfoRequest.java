package com.picktoss.picktossserver.domain.category.controller.request;

import com.picktoss.picktossserver.global.enums.CategoryTag;
import lombok.Getter;

@Getter
public class UpdateCategoryInfoRequest {
    private String name;
    private String emoji;
    private CategoryTag categoryTag;
}
