package com.picktoss.picktossserver.domain.collection.controller.mapper;

import com.picktoss.picktossserver.domain.collection.controller.dto.CollectionResponseDto;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionBookmark;
import com.picktoss.picktossserver.domain.collection.entity.CollectionSolvedRecord;
import com.picktoss.picktossserver.domain.member.entity.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CollectionDtoMapper {

    public static CollectionResponseDto collectionsToCollectionResponseDto(List<Collection> collections) {
        List<CollectionResponseDto.CollectionDto> collectionsDtos = new ArrayList<>();

        for (Collection collection : collections) {
            Set<CollectionBookmark> collectionBookmarks = collection.getCollectionBookmarks();
            boolean isBookmarked = collectionBookmarks.stream()
                    .anyMatch(bookmark -> bookmark.getCollection().equals(collection));

            int solvedMemberCount = (int) collection.getCollectionSolvedRecords().stream()
                    .map(CollectionSolvedRecord::getMember)
                    .map(Member::getId)
                    .distinct()
                    .count();

            Member createdMember = collection.getMember();

            CollectionResponseDto.CollectionMemberDto memberDto = CollectionResponseDto.CollectionMemberDto.builder()
                    .creatorId(createdMember.getId())
                    .creatorName(createdMember.getName())
                    .build();

            String collectionCategoryName = CollectionCategoryMapper.mapCollectionCategoryName(collection.getCollectionCategory());

            CollectionResponseDto.CollectionDto collectionDto = CollectionResponseDto.CollectionDto.builder()
                    .id(collection.getId())
                    .name(collection.getName())
                    .description(collection.getDescription())
                    .bookmarked(isBookmarked)
                    .emoji(collection.getEmoji())
                    .bookmarkCount(collection.getCollectionBookmarks().size())
                    .collectionCategory(collectionCategoryName)
                    .solvedMemberCount(solvedMemberCount)
                    .member(memberDto)
                    .build();

            collectionsDtos.add(collectionDto);
        }
        return new CollectionResponseDto(collectionsDtos);
    }
}
