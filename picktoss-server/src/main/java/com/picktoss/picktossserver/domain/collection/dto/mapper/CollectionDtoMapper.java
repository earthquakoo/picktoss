package com.picktoss.picktossserver.domain.collection.dto.mapper;

import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionBookmark;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuiz;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuizSet;
import com.picktoss.picktossserver.domain.member.entity.Member;

import java.util.*;

public class CollectionDtoMapper {

    public static CollectionResponseDto collectionsToCollectionResponseDto(List<Collection> collections, Long memberId) {
        List<CollectionResponseDto.CollectionDto> collectionsDtos = new ArrayList<>();

        for (Collection collection : collections) {
            boolean isBookmarked = false;
            Set<CollectionBookmark> collectionBookmarks = collection.getCollectionBookmarks();
            for (CollectionBookmark collectionBookmark : collectionBookmarks) {
                if (collectionBookmark.getMember().getId().equals(memberId)) {
                    isBookmarked = true;
                }
            }

            int solvedMemberCount = 0;

            Set<CollectionQuiz> collectionQuizzes = collection.getCollectionQuizzes();
            Map<Collection, CollectionQuizSet> collectionQuizMap = new HashMap<>();

            for (CollectionQuiz collectionQuiz : collectionQuizzes) {
                if (!collectionQuiz.getCollectionQuizSetCollectionQuizzes().isEmpty()) {
                    CollectionQuizSet collectionQuizSet = collectionQuiz.getCollectionQuizSetCollectionQuizzes().getFirst().getCollectionQuizSet();
                    collectionQuizMap.putIfAbsent(collectionQuiz.getCollection(), collectionQuizSet);
                }
            }

            for (CollectionQuizSet collectionQuizSet : collectionQuizMap.values()) {
                if (collectionQuizSet.isSolved()) {
                    solvedMemberCount += 1;
                }
            }

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
                    .totalQuizCount(collection.getCollectionQuizzes().size())
                    .member(memberDto)
                    .build();

            collectionsDtos.add(collectionDto);
        }
        return new CollectionResponseDto(collectionsDtos);
    }
}
