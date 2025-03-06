package com.picktoss.picktossserver.domain.collection.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class GetCollectionContainingQuiz {

    private List<GetCollectionContainingQuizDto> collections;

    @Getter
    @Builder
    public static class GetCollectionContainingQuizDto {
        private Long id;
        private String name;
        private String emoji;
        private String collectionCategory;
        private Boolean isQuizIncluded;
    }
}
