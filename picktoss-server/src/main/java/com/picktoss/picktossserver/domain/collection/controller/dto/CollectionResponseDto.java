package com.picktoss.picktossserver.domain.collection.controller.dto;


import com.picktoss.picktossserver.global.enums.CollectionDomain;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class CollectionResponseDto {

    private List<CollectionDto> collections;

    @Getter
    @Builder
    public static class CollectionDto {
        private Long id;
        private String name;
        private String emoji;
        private int bookmarkCount;
        private CollectionDomain collectionDomain;
        private String memberName;
        private int quizCount;
    }

}
