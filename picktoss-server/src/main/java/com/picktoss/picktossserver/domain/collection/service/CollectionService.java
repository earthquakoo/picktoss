package com.picktoss.picktossserver.domain.collection.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.collection.controller.response.GetCollectionSAnalysisResponse;
import com.picktoss.picktossserver.domain.collection.controller.request.UpdateCollectionQuizResultRequest;
import com.picktoss.picktossserver.domain.collection.controller.response.GetCollectionSolvedRecordResponse;
import com.picktoss.picktossserver.domain.collection.controller.response.GetSingleCollectionResponse;
import com.picktoss.picktossserver.domain.collection.entity.*;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.repository.*;
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
    private final CollectionSolvedRecordDetailRepository collectionSolvedRecordDetailRepository;

    @Transactional
    public void createCollection(
            List<Quiz> quizzes, String name, String description, String emoji, CollectionField collectionField, Member member) {

        List<CollectionQuiz> collectionQuizzes = new ArrayList<>();

        Collection collection = Collection.createCollection(name, emoji, description, collectionField, member);

        for (Quiz quiz : quizzes) {
            CollectionQuiz collectionQuiz = CollectionQuiz.createQuizCollection(quiz, collection);
            collectionQuizzes.add(collectionQuiz);
        }

        collectionRepository.save(collection);
        collectionQuizRepository.saveAll(collectionQuizzes);
    }

    // 탐색 컬렉션
    public List<Collection> findAllCollections(
            CollectionSortOption collectionSortOption, List<CollectionField> collectionFields, QuizType quizType, Integer quizCount) {

        List<Collection> collections;

        if (collectionFields == null) {
            collections = collectionRepository.findAllOrderByUpdatedAtDesc();
        } else {
            collections = collectionRepository.findAllByCollectionDomainsAndUpdatedAt(collectionFields);
        }


        if (quizCount == null && quizType == null) {
            return collections;
        }

        if (collectionSortOption == CollectionSortOption.POPULARITY) {
            collections.sort((c1, c2) ->
                    Integer.compare(c2.getCollectionBookmarks().size(), c1.getCollectionBookmarks().size())
            );
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

    // 북마크한 컬렉션 가져오기
    public List<Collection> findAllByMemberIdAndBookmarked(Long memberId) {
        return collectionRepository.findAllByMemberIdAndBookmarked(memberId);
    }

    // 직접 생성한 컬렉션 가져오기
    public List<Collection> findAllByMemberId(Long memberId) {
        return collectionRepository.findAllByMemberId(memberId);
    }

    // 만든 컬렉션 상세
    public GetSingleCollectionResponse findCollectionByCollectionId(Long collectionId, Long memberId) {
        Collection collection = collectionRepository.findCollectionWithCollectionSolvedRecordByCollectionIdAndMemberId(collectionId, memberId)
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
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .options(optionList)
                    .quizType(quiz.getQuizType())
                    .build();

            quizzesDtos.add(quizDto);
        }

        return GetSingleCollectionResponse.builder()
                .id(collection.getId())
                .name(collection.getName())
                .description(collection.getDescription())
                .solvedCount(collection.getCollectionSolvedRecords().size())
                .bookmarkCount(collection.getCollectionBookmarks().size())
                .quizzes(quizzesDtos)
                .build();
    }

    public GetCollectionSolvedRecordResponse findCollectionSolvedRecord(Long memberId, Long collectionId) {
        CollectionSolvedRecord collectionSolvedRecord = collectionSolvedRecordRepository.findByMemberIdAndCollectionId(memberId, collectionId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        List<GetCollectionSolvedRecordResponse.GetCollectionSolvedRecordDto> collectionSolvedRecordDtos = new ArrayList<>();

        List<CollectionSolvedRecordDetail> collectionSolvedRecordDetails = collectionSolvedRecord.getCollectionSolvedRecordDetails();
        int totalElapsedTimeMs = 0;
        for (CollectionSolvedRecordDetail collectionSolvedRecordDetail : collectionSolvedRecordDetails) {
            totalElapsedTimeMs += collectionSolvedRecordDetail.getElapsedTime();
            Quiz quiz = collectionSolvedRecordDetail.getQuiz();
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
            GetCollectionSolvedRecordResponse.GetCollectionSolvedRecordDto collectionSolvedRecordDto = GetCollectionSolvedRecordResponse.GetCollectionSolvedRecordDto.builder()
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .options(optionList)
                    .isAnswer(collectionSolvedRecordDetail.getIsAnswer())
                    .choseAnswer(collectionSolvedRecordDetail.getChoseAnswer())
                    .build();

            collectionSolvedRecordDtos.add(collectionSolvedRecordDto);
        }
        return new GetCollectionSolvedRecordResponse(collectionSolvedRecord.getCreatedAt(), totalElapsedTimeMs, collectionSolvedRecordDtos);
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

    @Transactional
    public void updateCollectionQuizResult(
            List<UpdateCollectionQuizResultRequest.UpdateCollectionQuizResultDto> collectionQuizDtos, Long collectionId, Member member) {
        Collection collection = collectionRepository.findCollectionWithCollectionQuizByCollectionId(collectionId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        CollectionSolvedRecord collectionSolvedRecord = CollectionSolvedRecord.createCollectionSolvedRecord(collection, member);

        List<CollectionSolvedRecordDetail> collectionSolvedRecordDetails = new ArrayList<>();
        Map<Long, UpdateCollectionQuizResultRequest.UpdateCollectionQuizResultDto> collectionQuizAndQuizIdMapping = new HashMap<>();
        for (UpdateCollectionQuizResultRequest.UpdateCollectionQuizResultDto collectionQuizDto : collectionQuizDtos) {
            collectionQuizAndQuizIdMapping.put(collectionQuizDto.getQuizId(), collectionQuizDto);
        }

        Set<CollectionQuiz> collectionQuizzes = collection.getCollectionQuizzes();

        for (CollectionQuiz collectionQuiz : collectionQuizzes) {
            Quiz quiz = collectionQuiz.getQuiz();
            UpdateCollectionQuizResultRequest.UpdateCollectionQuizResultDto collectionQuizDto = collectionQuizAndQuizIdMapping.get(quiz.getId());
            CollectionSolvedRecordDetail collectionSolvedRecordDetail = CollectionSolvedRecordDetail.createCollectionSolvedRecordDetail(
                    collectionQuizDto.getElapsedTimeMs(),
                    collectionQuizDto.getIsAnswer(),
                    collectionQuizDto.getChoseAnswer(),
                    collectionSolvedRecord,
                    quiz);

            collectionSolvedRecordDetails.add(collectionSolvedRecordDetail);
        }

        collectionSolvedRecordRepository.save(collectionSolvedRecord);
        collectionSolvedRecordDetailRepository.saveAll(collectionSolvedRecordDetails);
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

    public GetCollectionSAnalysisResponse findCollectionsAnalysis(Long memberId) {
        List<CollectionSolvedRecord> collectionSolvedRecords = collectionSolvedRecordRepository.findAllByMemberId(memberId);
        Map<CollectionField, Integer> collectionFieldMap = new HashMap<>();

        for (CollectionSolvedRecord collectionSolvedRecord : collectionSolvedRecords) {
            Collection collection = collectionSolvedRecord.getCollection();
            collectionFieldMap.put(collection.getCollectionField(), collectionFieldMap.getOrDefault(collection.getCollectionField(), 0) + 1);
        }

        return GetCollectionSAnalysisResponse.builder()
                .collectionsAnalysis(collectionFieldMap)
                .build();
    }

    @Transactional
    public void createCollectionBookmark(Member member, Long collectionId) {
        Collection collection = collectionRepository.findCollectionById(collectionId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        Long collectionMemberId = collection.getMember().getId();

        if (Objects.equals(member.getId(), collectionMemberId)) {
            throw new CustomException(OWN_COLLECTION_CANT_BOOKMARK);
        }

        CollectionBookmark collectionBookmark = CollectionBookmark.createCollectionBookmark(collection, member);

        collectionBookmarkRepository.save(collectionBookmark);
    }

    @Transactional
    public void deleteCollectionBookmark(Long memberId, Long collectionId) {
        CollectionBookmark collectionBookmark = collectionBookmarkRepository.findByMemberIdAndCollectionId(memberId, collectionId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        collectionBookmarkRepository.delete(collectionBookmark);
    }

    public List<Collection> findInterestFieldCollections(List<String> collectionFields) {
        List<CollectionField> interestCollectionFields = new ArrayList<>();
        for (String collectionFieldString : collectionFields) {
            CollectionField collectionField = CollectionField.valueOf(collectionFieldString);
            interestCollectionFields.add(collectionField);
        }
        return collectionRepository.findAllByCollectionDomainsAndUpdatedAt(interestCollectionFields);
    }
}
