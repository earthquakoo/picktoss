package com.picktoss.picktossserver.domain.collection.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.collection.controller.response.GetAllCollectionsResponse;
import com.picktoss.picktossserver.domain.collection.controller.response.GetCollectionSAnalysisResponse;
import com.picktoss.picktossserver.domain.collection.controller.response.GetQuizzesInCollectionByCollectionField;
import com.picktoss.picktossserver.domain.collection.controller.response.GetSingleCollectionResponse;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionBookmark;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuiz;
import com.picktoss.picktossserver.domain.collection.entity.CollectionSolvedRecord;
import com.picktoss.picktossserver.domain.collection.repository.CollectionBookmarkRepository;
import com.picktoss.picktossserver.domain.collection.repository.CollectionQuizRepository;
import com.picktoss.picktossserver.domain.collection.repository.CollectionRepository;
import com.picktoss.picktossserver.domain.collection.repository.CollectionSolvedRecordRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.quiz.entity.Option;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.global.enums.collection.CollectionField;
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
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionQuizRepository collectionQuizRepository;
    private final CollectionBookmarkRepository collectionBookmarkRepository;
    private final CollectionSolvedRecordRepository collectionSolvedRecordRepository;

    @Transactional
    public Long createCollection(
            List<Quiz> quizzes, String name, String description, String emoji, CollectionField collectionField, Member member) {

        List<CollectionQuiz> collectionQuizzes = new ArrayList<>();

        Collection collection = Collection.createCollection(name, emoji, description, collectionField, member);

        for (Quiz quiz : quizzes) {
            CollectionQuiz collectionQuiz = CollectionQuiz.createQuizCollection(quiz, collection);
            collectionQuizzes.add(collectionQuiz);
        }

        collectionRepository.save(collection);
        collectionQuizRepository.saveAll(collectionQuizzes);
        return collection.getId();
    }

    // 탐색 컬렉션
    public GetAllCollectionsResponse findAllCollections(
            CollectionSortOption collectionSortOption, List<CollectionField> collectionFields, QuizType quizType, Integer quizCount) {

        List<Collection> collections = filterCollections(collectionSortOption, collectionFields, quizType, quizCount);

        List<GetAllCollectionsResponse.GetAllCollectionsDto> collectionsDtos = new ArrayList<>();

        for (Collection collection : collections) {
            Set<CollectionQuiz> collectionQuizzes = collection.getCollectionQuizzes();
            List<GetAllCollectionsResponse.GetAllCollectionsQuizDto> quizDtos = new ArrayList<>();
            for (CollectionQuiz collectionQuiz : collectionQuizzes) {
                Quiz quiz = collectionQuiz.getQuiz();
                List<String> optionList = new ArrayList<>();
                if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                    Set<Option> options = quiz.getOptions();
                    for (Option option : options) {
                        optionList.add(option.getOption());
                    }
                }
                GetAllCollectionsResponse.GetAllCollectionsQuizDto quizDto = GetAllCollectionsResponse.GetAllCollectionsQuizDto.builder()
                        .id(quiz.getId())
                        .question(quiz.getQuestion())
                        .answer(quiz.getAnswer())
                        .explanation(quiz.getExplanation())
                        .options(optionList)
                        .quizType(quiz.getQuizType())
                        .build();

                quizDtos.add(quizDto);
            }

            int solvedMemberCount = findSolvedCountCollectionByCollectionId(collection);

            Member createdMember = collection.getMember();

            GetAllCollectionsResponse.GetAllCollectionsMemberDto memberDto = GetAllCollectionsResponse.GetAllCollectionsMemberDto.builder()
                    .creatorId(createdMember.getId())
                    .creatorName(createdMember.getName())
                    .build();

            GetAllCollectionsResponse.GetAllCollectionsDto collectionDto = GetAllCollectionsResponse.GetAllCollectionsDto.builder()
                    .id(collection.getId())
                    .name(collection.getName())
                    .description(collection.getDescription())
                    .emoji(collection.getEmoji())
                    .bookmarkCount(collection.getCollectionBookmarks().size())
                    .collectionField(collection.getCollectionField())
                    .solvedMemberCount(solvedMemberCount)
                    .member(memberDto)
                    .quizzes(quizDtos)
                    .build();

            collectionsDtos.add(collectionDto);
        }
        return new GetAllCollectionsResponse(collectionsDtos);
    }

    // 북마크한 컬렉션 가져오기
    public List<Collection> findAllByMemberIdAndBookmarked(Long memberId) {
        return collectionRepository.findAllByMemberIdAndBookmarked(memberId);
    }

    // 컬렉션의 카테고리별로 모든 퀴즈 가져오기
    public List<GetQuizzesInCollectionByCollectionField.QuizInCollectionDto> findAllByMemberIdAndCollectionFieldAndBookmarked(Long memberId, CollectionField collectionField) {
        List<CollectionQuiz> collectionQuizzes = collectionQuizRepository.findQuizzesInCollectionByMemberIdAndBookmarkedAndCollectionField(memberId, collectionField);
        List<GetQuizzesInCollectionByCollectionField.QuizInCollectionDto> quizDtos = new ArrayList<>();
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

            GetQuizzesInCollectionByCollectionField.QuizInCollectionDto quizDto = GetQuizzesInCollectionByCollectionField.QuizInCollectionDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .quizType(quiz.getQuizType())
                    .options(optionList)
                    .build();

            quizDtos.add(quizDto);
        }
        return quizDtos;
    }

    // 직접 생성한 컬렉션 가져오기
    public GetAllCollectionsResponse findMemberGeneratedCollections(Long memberId) {
        List<Collection> collections = collectionRepository.findAllByMemberId(memberId);

        List<GetAllCollectionsResponse.GetAllCollectionsDto> collectionsDtos = new ArrayList<>();

        for (Collection collection : collections) {
            Set<CollectionQuiz> collectionQuizzes = collection.getCollectionQuizzes();
            List<GetAllCollectionsResponse.GetAllCollectionsQuizDto> quizDtos = new ArrayList<>();
            for (CollectionQuiz collectionQuiz : collectionQuizzes) {
                Quiz quiz = collectionQuiz.getQuiz();
                List<String> optionList = new ArrayList<>();
                if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                    Set<Option> options = quiz.getOptions();
                    for (Option option : options) {
                        optionList.add(option.getOption());
                    }
                }
                GetAllCollectionsResponse.GetAllCollectionsQuizDto quizDto = GetAllCollectionsResponse.GetAllCollectionsQuizDto.builder()
                        .id(quiz.getId())
                        .question(quiz.getQuestion())
                        .answer(quiz.getAnswer())
                        .explanation(quiz.getExplanation())
                        .options(optionList)
                        .quizType(quiz.getQuizType())
                        .build();

                quizDtos.add(quizDto);
            }

            int solvedMemberCount = findSolvedCountCollectionByCollectionId(collection);

            Member createdMember = collection.getMember();

            GetAllCollectionsResponse.GetAllCollectionsMemberDto memberDto = GetAllCollectionsResponse.GetAllCollectionsMemberDto.builder()
                    .creatorId(createdMember.getId())
                    .creatorName(createdMember.getName())
                    .build();

            GetAllCollectionsResponse.GetAllCollectionsDto collectionDto = GetAllCollectionsResponse.GetAllCollectionsDto.builder()
                    .id(collection.getId())
                    .name(collection.getName())
                    .description(collection.getDescription())
                    .emoji(collection.getEmoji())
                    .bookmarkCount(collection.getCollectionBookmarks().size())
                    .collectionField(collection.getCollectionField())
                    .solvedMemberCount(solvedMemberCount)
                    .member(memberDto)
                    .quizzes(quizDtos)
                    .build();

            collectionsDtos.add(collectionDto);
        }
        return new GetAllCollectionsResponse(collectionsDtos);
    }

    // 만든 컬렉션 상세
    public GetSingleCollectionResponse findCollectionByCollectionIdAndMemberId(Long collectionId, Long memberId) {
        Collection collection = collectionRepository.findCollectionWithSolvedRecordAndBookmarkAndQuizzesByCollectionIdAndMemberId(collectionId, memberId)
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

        int solvedMemberCount = findSolvedCountCollectionByCollectionId(collection);

        return GetSingleCollectionResponse.builder()
                .id(collection.getId())
                .name(collection.getName())
                .description(collection.getDescription())
                .emoji(collection.getEmoji())
                .collectionField(collection.getCollectionField())
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

    @Transactional
    public void deleteCollection(Long collectionId, Long memberId) {
        Collection collection = collectionRepository.findCollectionByCollectionIdAndMemberId(collectionId, memberId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        collectionRepository.delete(collection);
    }

    // 컬렉션 정보 수정
    @Transactional
    public void updateCollectionInfo(
            Long collectionId, Long memberId, String name, String description, String emoji, CollectionField collectionField) {
        Collection collection = collectionRepository.findCollectionByCollectionIdAndMemberId(collectionId, memberId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        collection.updateCollectionByUpdateCollectionInfo(name, description, emoji, collectionField);
    }

    // 컬렉션에 퀴즈 추가
    @Transactional
    public void addQuizToCollection(Long collectionId, Long memberId, Quiz quiz) {
        Collection collection = collectionRepository.findCollectionByCollectionIdAndMemberId(collectionId, memberId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        Set<CollectionQuiz> collectionQuizzes = collection.getCollectionQuizzes();
        for (CollectionQuiz collectionQuiz : collectionQuizzes) {
            if (Objects.equals(collectionQuiz.getQuiz().getId(), quiz.getId())) {
                throw new CustomException(DUPLICATE_QUIZ_IN_COLLECTION);
            }
        }

        CollectionQuiz collectionQuiz = CollectionQuiz.createQuizCollection(quiz, collection);
        collectionQuizRepository.save(collectionQuiz);
    }

    // 컬렉션 퀴즈 편집
    @Transactional
    public void updateCollectionQuizzes(List<Quiz> quizzes, Long collectionId, Long memberId) {
        Collection collection = collectionRepository.findCollectionByCollectionIdAndMemberId(collectionId, memberId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        Set<CollectionQuiz> curCollectionQuizzes = collection.getCollectionQuizzes();
        collectionQuizRepository.deleteAll(curCollectionQuizzes);

        List<CollectionQuiz> newCollectionQuizzes = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            CollectionQuiz collectionQuiz = CollectionQuiz.createQuizCollection(quiz, collection);
            newCollectionQuizzes.add(collectionQuiz);
        }

        collectionQuizRepository.saveAll(newCollectionQuizzes);
    }

    // 컬렉션 북마크
    @Transactional
    public void createCollectionBookmark(Member member, Long collectionId) {
        Collection collection = collectionRepository.findCollectionById(collectionId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        Long collectionMemberId = collection.getMember().getId();

        if (Objects.equals(member.getId(), collectionMemberId)) {
            throw new CustomException(OWN_COLLECTION_CANT_BOOKMARK);
        }

        Set<CollectionBookmark> collectionBookmarks = collection.getCollectionBookmarks();
        for (CollectionBookmark collectionBookmark : collectionBookmarks) {
            if (Objects.equals(collectionId, collectionBookmark.getId())) {
                throw new CustomException(COLLECTION_ALREADY_BOOKMARKED);
            }
        }

        CollectionBookmark collectionBookmark = CollectionBookmark.createCollectionBookmark(collection, member);

        collectionBookmarkRepository.save(collectionBookmark);
    }

    // 컬렉션 북마크 해제
    @Transactional
    public void deleteCollectionBookmark(Member member, Collection collection) {
        CollectionBookmark collectionBookmark = collectionBookmarkRepository.findByMemberAndCollection(member, collection)
                .orElseThrow(() -> new CustomException(COLLECTION_BOOKMARK_NOT_FOUND));

        collectionBookmarkRepository.delete(collectionBookmark);
    }

    // 사용자 관심 분야 컬렉션
    public List<Collection> findInterestFieldCollections(List<String> collectionFields) {
        List<CollectionField> interestCollectionFields = new ArrayList<>();
        for (String collectionFieldString : collectionFields) {
            CollectionField collectionField = CollectionField.valueOf(collectionFieldString);
            interestCollectionFields.add(collectionField);
        }
        return collectionRepository.findAllByCollectionDomainsAndUpdatedAt(interestCollectionFields);
    }

    public Collection findCollectionByCollectionId(Long collectionId) {
        return collectionRepository.findCollectionWithCollectionQuizByCollectionId(collectionId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));
    }

    public GetCollectionSAnalysisResponse findCollectionsAnalysis(Long memberId) {
        List<CollectionSolvedRecord> collectionSolvedRecords = collectionSolvedRecordRepository.findAllByMemberId(memberId);

        Map<Collection, CollectionField> quizMap = new HashMap<>();
        // 중복된 컬렉션 제거 후 map으로 변경
        for (CollectionSolvedRecord collectionSolvedRecord : collectionSolvedRecords) {
            Collection collection = collectionSolvedRecord.getCollection();
            quizMap.putIfAbsent(collection, collection.getCollectionField());
        }

        Map<CollectionField, Integer> collectionFieldMap = new HashMap<>();
        for (Collection collection : quizMap.keySet()) {
            collectionFieldMap.put(collection.getCollectionField(), collectionFieldMap.getOrDefault(collection.getCollectionField(), 0) + 1);
        }
        return GetCollectionSAnalysisResponse.builder()
                .collectionsAnalysis(collectionFieldMap)
                .build();
    }

    private int findSolvedCountCollectionByCollectionId(Collection collection) {
        return (int) collection.getCollectionSolvedRecords().stream()
                .map(CollectionSolvedRecord::getMember)
                .map(Member::getId)
                .distinct()
                .count();
    }

    private List<Collection> filterCollections(CollectionSortOption collectionSortOption, List<CollectionField> collectionFields, QuizType quizType, Integer quizCount) {
        List<Collection> collections;

        if (collectionFields == null) {
            collections = collectionRepository.findAllOrderByUpdatedAtDesc();
        } else {
            collections = collectionRepository.findAllByCollectionDomainsAndUpdatedAt(collectionFields);
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
