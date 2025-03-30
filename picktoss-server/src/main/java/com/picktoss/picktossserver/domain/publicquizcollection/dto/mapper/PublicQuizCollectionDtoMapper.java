package com.picktoss.picktossserver.domain.publicquizcollection.dto.mapper;

import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.publicquizcollection.entity.PublicQuizCollection;

import java.util.ArrayList;
import java.util.List;

public class PublicQuizCollectionDtoMapper {

    public static PublicQuizCollectionResponseDto collectionsToPublicQuizCollectionDto(List<PublicQuizCollection> publicQuizCollections) {
        List<PublicQuizCollectionResponseDto.PublicQuizCollectionsDto> publicQuizCollectionsDtos = new ArrayList<>();

        for (PublicQuizCollection publicQuizCollection : publicQuizCollections) {
            Document document = publicQuizCollection.getDocument();

            boolean isBookmarked = publicQuizCollection.getPublicQuizCollectionBookmarks().stream()
                    .anyMatch(bookmark -> bookmark.getPublicQuizCollection().equals(publicQuizCollection));

            PublicQuizCollectionResponseDto.PublicQuizCollectionsDto publicQuizCollectionsDto = PublicQuizCollectionResponseDto.PublicQuizCollectionsDto.builder()
                    .id(publicQuizCollection.getId())
                    .title(document.getName())
                    .explanation(publicQuizCollection.getExplanation())
                    .emoji(document.getEmoji())
                    .publicQuizCollectionCategory(publicQuizCollection.getPublicQuizCollectionCategory())
                    .quizCount(document.getQuizzes().size())
                    .tryCount(publicQuizCollection.getTryCount())
                    .isBookmarked(isBookmarked)
                    .build();

            publicQuizCollectionsDtos.add(publicQuizCollectionsDto);
        }

        return new PublicQuizCollectionResponseDto(publicQuizCollectionsDtos);
    }
}
