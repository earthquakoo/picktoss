package com.picktoss.picktossserver.domain.category.controller.request;

import com.picktoss.picktossserver.global.enums.category.CategoryTag;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class CreateCategoryRequest {

    private String name;
    private String emoji;
}
