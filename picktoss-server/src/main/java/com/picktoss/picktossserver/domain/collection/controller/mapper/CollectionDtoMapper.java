package com.picktoss.picktossserver.domain.collection.controller.mapper;

import com.picktoss.picktossserver.domain.collection.controller.dto.CollectionResponseDto;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionBookmark;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuiz;
import com.picktoss.picktossserver.domain.collection.entity.CollectionSolvedRecord;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.quiz.entity.Option;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class CollectionDtoMapper {

    public static CollectionResponseDto collectionsToCollectionResponseDto(List<Collection> collections) {
        List<CollectionResponseDto.CollectionQuizDto> collectionsDtos = new ArrayList<>();

        for (Collection collection : collections) {
            Set<CollectionBookmark> collectionBookmarks = collection.getCollectionBookmarks();
            boolean isBookmarked = collectionBookmarks.stream()
                    .anyMatch(bookmark -> bookmark.getCollection().equals(collection));
            Set<CollectionQuiz> collectionQuizzes = collection.getCollectionQuizzes();
            List<CollectionResponseDto.CollectionQuizzesDto> quizDtos = new ArrayList<>();
            for (CollectionQuiz collectionQuiz : collectionQuizzes) {
                Quiz quiz = collectionQuiz.getQuiz();
                List<String> optionList = new ArrayList<>();
                if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                    Set<Option> options = quiz.getOptions();
                    for (Option option : options) {
                        optionList.add(option.getOption());
                    }
                }
                CollectionResponseDto.CollectionQuizzesDto quizDto = CollectionResponseDto.CollectionQuizzesDto.builder()
                        .id(quiz.getId())
                        .question(quiz.getQuestion())
                        .answer(quiz.getAnswer())
                        .explanation(quiz.getExplanation())
                        .options(optionList)
                        .quizType(quiz.getQuizType())
                        .build();

                quizDtos.add(quizDto);
            }

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

            CollectionResponseDto.CollectionQuizDto collectionDto = CollectionResponseDto.CollectionQuizDto.builder()
                    .id(collection.getId())
                    .name(collection.getName())
                    .description(collection.getDescription())
                    .isBookmarked(isBookmarked)
                    .emoji(collection.getEmoji())
                    .bookmarkCount(collection.getCollectionBookmarks().size())
                    .collectionField(collection.getCollectionField())
                    .solvedMemberCount(solvedMemberCount)
                    .member(memberDto)
                    .quizzes(quizDtos)
                    .build();

            collectionsDtos.add(collectionDto);
        }
        return new CollectionResponseDto(collectionsDtos);
    }
}
