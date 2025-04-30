package com.picktoss.picktossserver.domain.member.dto.request;

import com.picktoss.picktossserver.domain.category.entity.Category;
import lombok.Getter;

@Getter
public class UpdateMemberCategoryRequest {
    private Long categoryId;
}
