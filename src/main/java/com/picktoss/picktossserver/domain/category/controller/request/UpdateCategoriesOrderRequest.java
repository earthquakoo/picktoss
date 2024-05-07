package com.picktoss.picktossserver.domain.category.controller.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UpdateCategoriesOrderRequest {

    private List<UpdateCategoryDto> categories;

    @Getter
    public static class UpdateCategoryDto {
        private Long id;
        private int order;
    }
}
