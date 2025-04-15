package com.picktoss.picktossserver.domain.category.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class GetAllCategoriesResponse {

    private List<GetAllCategoriesDto> categories;

    @Getter
    @Builder
    public static class GetAllCategoriesDto {
        private Long id;
        private String name;
        private String emoji;
    }
}
