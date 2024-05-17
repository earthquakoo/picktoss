package com.picktoss.picktossserver.domain.category.controller.request;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class UpdateCategoriesOrderRequest {

    private Long categoryId;
    private int preDragCategoryOrder;
    private int afterDragCategoryOrder;
}
