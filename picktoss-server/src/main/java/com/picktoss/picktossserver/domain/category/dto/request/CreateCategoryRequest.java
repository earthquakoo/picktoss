package com.picktoss.picktossserver.domain.category.dto.request;

import lombok.Getter;

@Getter
public class CreateCategoryRequest {
    private String name;
    private String emoji;
}
