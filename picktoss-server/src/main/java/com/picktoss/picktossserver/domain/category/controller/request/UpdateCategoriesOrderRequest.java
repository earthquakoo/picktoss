package com.picktoss.picktossserver.domain.category.controller.request;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateCategoriesOrderRequest {

    private Long categoryId;
    private int preDragCategoryOrder;
    private int afterDragCategoryOrder;
}
