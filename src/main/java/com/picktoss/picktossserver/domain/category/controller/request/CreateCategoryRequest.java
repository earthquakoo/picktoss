package com.picktoss.picktossserver.domain.category.controller.request;

import com.picktoss.picktossserver.global.enums.CategoryTag;
import lombok.Getter;

@Getter
public class CreateCategoryRequest {

    private String name;
    private CategoryTag tag;
}
