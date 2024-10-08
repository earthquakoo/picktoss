package com.picktoss.picktossserver.domain.collection.controller.mapper;

import com.picktoss.picktossserver.domain.collection.controller.dto.CollectionResponseDto;
import com.picktoss.picktossserver.domain.collection.entity.Collection;

import java.util.ArrayList;
import java.util.List;

public class CollectionMapper {

    public static CollectionResponseDto collectionsToCollectionResponseDto(List<Collection> collections) {

        List<CollectionResponseDto.CollectionDto> collectionDtos = new ArrayList<>();

        for (Collection collection : collections) {
            int collectionQuizCount = collection.getCollectionQuizzes().size();
            int bookmarkCount = collection.getCollectionBookmarks().size();
            CollectionResponseDto.CollectionDto collectionDto = CollectionResponseDto.CollectionDto.builder()
                    .id(collection.getId())
                    .name(collection.getName())
                    .emoji(collection.getEmoji())
                    .bookmarkCount(bookmarkCount)
                    .memberName(collection.getMember().getName())
                    .quizCount(collectionQuizCount)
                    .build();

            collectionDtos.add(collectionDto);
        }

        return new CollectionResponseDto(collectionDtos);
    }
}
