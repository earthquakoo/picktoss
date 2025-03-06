package com.picktoss.picktossserver.domain.collection.dto.response;

import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetCollectionCategoriesResponse {

    private List<GetCollectionCategoriesDto> collectionCategories;

    @Getter
    @Builder
    public static class GetCollectionCategoriesDto {
        private CollectionCategory collectionCategory;
        private String categoryName;
        private String emoji;
        private List<GetCollectionCategoriesCollectionDto> collections;
    }

    @Getter
    @Builder
    public static class GetCollectionCategoriesCollectionDto {
        private Long id;
        private String name;
    }
}
