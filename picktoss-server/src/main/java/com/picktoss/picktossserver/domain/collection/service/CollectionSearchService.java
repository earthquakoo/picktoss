package com.picktoss.picktossserver.domain.collection.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.collection.dto.mapper.CollectionCategoryMapper;
import com.picktoss.picktossserver.domain.collection.dto.response.GetCollectionCategoriesResponse;
import com.picktoss.picktossserver.domain.collection.dto.response.GetQuizzesInCollectionByCollectionCategory;
import com.picktoss.picktossserver.domain.collection.dto.response.GetSingleCollectionResponse;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionBookmark;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuiz;
import com.picktoss.picktossserver.domain.collection.repository.CollectionQuizRepository;
import com.picktoss.picktossserver.domain.collection.repository.CollectionRepository;
import com.picktoss.picktossserver.domain.collection.util.CollectionUtil;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.quiz.entity.Option;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import com.picktoss.picktossserver.global.enums.collection.CollectionSortOption;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionSearchService {

    private final CollectionRepository collectionRepository;
    private final CollectionQuizRepository collectionQuizRepository;
    private final MemberRepository memberRepository;
    private final CollectionUtil collectionUtil;


    public List<Collection> findAllCollections(
            CollectionSortOption collectionSortOption, List<CollectionCategory> collectionCategories, QuizType quizType, Integer quizCount) {

        return filterCollections(collectionSortOption, collectionCategories, quizType, quizCount);
    }

    public List<Collection> findAllByMemberIdAndBookmarked(Long memberId) {
        return collectionRepository.findAllByMemberIdAndBookmarked(memberId);
    }

    // 컬렉션의 카테고리별로 모든 퀴즈 가져오기
    public List<GetQuizzesInCollectionByCollectionCategory.QuizInCollectionDto> findAllByMemberIdAndCollectionCategoryAndBookmarked(Long memberId, CollectionCategory collectionCategory) {
        List<CollectionQuiz> collectionQuizzes = collectionQuizRepository.findQuizzesInCollectionByMemberIdOrBookmarkedAndCollectionField(memberId, collectionCategory);
        List<GetQuizzesInCollectionByCollectionCategory.QuizInCollectionDto> quizDtos = new ArrayList<>();
        for (CollectionQuiz collectionQuiz : collectionQuizzes) {
            Quiz quiz = collectionQuiz.getQuiz();

            List<String> optionList = new ArrayList<>();
            if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                Set<Option> options = quiz.getOptions();
                if (options.isEmpty()) {
                    continue;
                }
                for (Option option : options) {
                    optionList.add(option.getOption());
                }
            }

            Collection collection = collectionQuiz.getCollection();
            GetQuizzesInCollectionByCollectionCategory.QuizInCollectionByCollectionDto collectionDto = GetQuizzesInCollectionByCollectionCategory.QuizInCollectionByCollectionDto.builder()
                    .id(collection.getId())
                    .name(collection.getName())
                    .build();

            GetQuizzesInCollectionByCollectionCategory.QuizInCollectionDto quizDto = GetQuizzesInCollectionByCollectionCategory.QuizInCollectionDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .quizType(quiz.getQuizType())
                    .options(optionList)
                    .collection(collectionDto)
                    .build();

            quizDtos.add(quizDto);
        }
        return quizDtos;
    }

    public List<Collection> findMemberGeneratedCollections(Long memberId) {
        return collectionRepository.findAllByMemberId(memberId);
    }

    // 만든 컬렉션 상세
    public GetSingleCollectionResponse findCollectionInfoByCollectionId(Long collectionId) {
        Collection collection = collectionRepository.findCollectionWithSolvedRecordAndBookmarkAndQuizzesByCollectionId(collectionId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        List<GetSingleCollectionResponse.GetSingleCollectionQuizDto> quizzesDtos = new ArrayList<>();

        Set<CollectionQuiz> collectionQuizzes = collection.getCollectionQuizzes();
        for (CollectionQuiz collectionQuiz : collectionQuizzes) {
            Quiz quiz = collectionQuiz.getQuiz();
            List<String> optionList = new ArrayList<>();
            if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                Set<Option> options = quiz.getOptions();
                for (Option option : options) {
                    optionList.add(option.getOption());
                }
            }
            GetSingleCollectionResponse.GetSingleCollectionQuizDto quizDto = GetSingleCollectionResponse.GetSingleCollectionQuizDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .options(optionList)
                    .quizType(quiz.getQuizType())
                    .build();

            quizzesDtos.add(quizDto);
        }

        Member createdMember = collection.getMember();

        GetSingleCollectionResponse.GetSingleCollectionMemberDto memberDto = GetSingleCollectionResponse.GetSingleCollectionMemberDto.builder()
                .creatorId(createdMember.getId())
                .creatorName(createdMember.getName())
                .build();

        int solvedMemberCount = collectionUtil.countCollectionSolvedMembers(collection);
        String collectionCategoryName = CollectionCategoryMapper.mapCollectionCategoryName(collection.getCollectionCategory());
        Set<CollectionBookmark> collectionBookmarks = collection.getCollectionBookmarks();
        boolean isBookmarked = collectionBookmarks.stream()
                .anyMatch(bookmark -> bookmark.getCollection().equals(collection));

        return GetSingleCollectionResponse.builder()
                .id(collection.getId())
                .name(collection.getName())
                .description(collection.getDescription())
                .bookmarked(isBookmarked)
                .emoji(collection.getEmoji())
                .collectionCategory(collectionCategoryName)
                .solvedMemberCount(solvedMemberCount)
                .bookmarkCount(collection.getCollectionBookmarks().size())
                .member(memberDto)
                .quizzes(quizzesDtos)
                .build();
    }

    // 컬렉션 키워드 검색
    public List<Collection> searchCollections(String keyword) {
        return collectionRepository.findByCollectionContaining(keyword);
    }

    // 사용자 관심 분야 컬렉션
    public List<Collection> findInterestCategoryCollections(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        List<String> collectionFields = member.getInterestCollectionCategories();
        if (collectionFields == null) {
            collectionFields = new ArrayList<>();
        }

        List<CollectionCategory> interestCollectionCategories = new ArrayList<>();
        for (String collectionFieldString : collectionFields) {
            CollectionCategory collectionCategory = CollectionCategory.valueOf(collectionFieldString);
            interestCollectionCategories.add(collectionCategory);
        }
        return collectionRepository.findAllByCollectionDomainsAndUpdatedAt(interestCollectionCategories);
    }

    // 사용자가 북마크했거나 생성한 컬렉션 카테고리
    public List<GetCollectionCategoriesResponse.GetCollectionCategoriesDto> findCollectionCategoriesByMemberId(Long memberId) {
        List<Collection> collections = collectionRepository.findAllByMemberIdOrBookmarked(memberId);
        Map<CollectionCategory, List<Collection>> collectionCategoryListMap = new HashMap<>();

        for (Collection collection : collections) {
            CollectionCategory collectionCategory = collection.getCollectionCategory();

            if (!collectionCategoryListMap.containsKey(collectionCategory)) {
                collectionCategoryListMap.put(collectionCategory, new ArrayList<>());
            }
            collectionCategoryListMap.get(collectionCategory).add(collection);
        }

        List<GetCollectionCategoriesResponse.GetCollectionCategoriesDto> collectionCategoriesDtos = new ArrayList<>();

        for (CollectionCategory collectionCategory : collectionCategoryListMap.keySet()) {
            List<GetCollectionCategoriesResponse.GetCollectionCategoriesCollectionDto> collectionDtos = new ArrayList<>();

            List<Collection> categoryMappingCollections = collectionCategoryListMap.get(collectionCategory);
            for (Collection collection : categoryMappingCollections) {
                GetCollectionCategoriesResponse.GetCollectionCategoriesCollectionDto collectionDto = GetCollectionCategoriesResponse.GetCollectionCategoriesCollectionDto.builder()
                        .id(collection.getId())
                        .name(collection.getName())
                        .isDeleted(collection.getIsDeleted())
                        .build();

                collectionDtos.add(collectionDto);
            }


            String collectionCategoryName = CollectionCategoryMapper.mapCollectionCategoryName(collectionCategory);
            String collectionCategoryEmoji = CollectionCategoryMapper.mapCollectionCategoryEmoji(collectionCategory);
            GetCollectionCategoriesResponse.GetCollectionCategoriesDto collectionCategoriesDto = GetCollectionCategoriesResponse.GetCollectionCategoriesDto.builder()
                    .collectionCategory(collectionCategory)
                    .categoryName(collectionCategoryName)
                    .emoji(collectionCategoryEmoji)
                    .collections(collectionDtos)
                    .build();

            collectionCategoriesDtos.add(collectionCategoriesDto);
        }

        return collectionCategoriesDtos;
    }

    public void checkQuizInCollection(Long collectionId, Long quizId) {
        Collection collection = collectionRepository.findCollectionWithCollectionQuizByCollectionId(collectionId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        Set<CollectionQuiz> collectionQuizzes = collection.getCollectionQuizzes();
        for (CollectionQuiz collectionQuiz : collectionQuizzes) {
            if (collectionQuiz.getQuiz().getId().equals(quizId)) {
                throw new CustomException(DUPLICATE_QUIZ_IN_COLLECTION);
            }
        }
    }


    private List<Collection> filterCollections(CollectionSortOption collectionSortOption, List<CollectionCategory> collectionCategories, QuizType quizType, Integer quizCount) {
        List<Collection> collections;

        if (collectionCategories == null) {
            collections = collectionRepository.findAllOrderByUpdatedAtDesc();
        } else {
            collections = collectionRepository.findAllByCollectionDomainsAndUpdatedAt(collectionCategories);
        }

        if (collectionSortOption == CollectionSortOption.POPULARITY) {
            collections.sort((c1, c2) ->
                    Integer.compare(c2.getCollectionBookmarks().size(), c1.getCollectionBookmarks().size())
            );
        }

        if (quizCount == null && quizType == null) {
            return collections;
        }

        // 퀴즈 타입에 따른 필터링
        if (quizType != null) {
            collections = collections.stream()
                    .filter(collection -> collection.getCollectionQuizzes().stream()
                            .allMatch(collectionQuiz -> collectionQuiz.getQuiz().getQuizType() == quizType))
                    .collect(Collectors.toList());
        }

        // 퀴즈 개수에 따른 필터링
        if (quizCount != null) {
            collections = collections.stream()
                    .filter(collection -> collection.getCollectionQuizzes().size() >= quizCount)
                    .collect(Collectors.toList());
        }

        return collections;
    }
}
