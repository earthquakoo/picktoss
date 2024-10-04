package com.picktoss.picktossserver.domain.category.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class CategoryResponseDto {

    private Long id;
    private String name;
}
