package com.picktoss.picktossserver.domain.collection.dto.mapper;

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
        private String description;
        private String emoji;
        private int bookmarkCount;
        private String collectionCategory;
        private int solvedMemberCount;
        private boolean bookmarked;
        private int totalQuizCount;
        private CollectionMemberDto member;
    }

    @Getter
    @Builder
    public static class CollectionMemberDto {
        private Long creatorId;
        private String creatorName;
    }
}
