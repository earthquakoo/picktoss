package com.picktoss.picktossserver.domain.collection.facade;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.collection.controller.response.GetAllCollectionsResponse;
import com.picktoss.picktossserver.domain.collection.controller.response.GetCollectionSAnalysisResponse;
import com.picktoss.picktossserver.domain.collection.controller.response.GetQuizzesInCollectionByCollectionField;
import com.picktoss.picktossserver.domain.collection.controller.response.GetSingleCollectionResponse;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.service.CollectionService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.service.QuizService;
import com.picktoss.picktossserver.global.enums.collection.CollectionField;
import com.picktoss.picktossserver.global.enums.collection.CollectionSortOption;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.QUIZ_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionFacade {

    private final CollectionService collectionService;
    private final QuizService quizService;
    private final MemberService memberService;

    @Transactional
    public Long createCollection(
            List<Long> quizIds, String name, String description, String emoji, CollectionField collectionType, Long memberId) {
        List<Quiz> quizzes = quizService.findAllByMemberIdAndQuizIds(quizIds, memberId);
        if (quizzes.isEmpty()) {
            throw new CustomException(QUIZ_NOT_FOUND_ERROR);
        }
        Member member = memberService.findMemberById(memberId);
        return collectionService.createCollection(quizzes, name, description, emoji, collectionType, member);
    }

    // 탐색 컬렉션
    public GetAllCollectionsResponse findAllCollections(
            CollectionSortOption collectionSortOption, List<CollectionField> collectionFields, QuizType quizType, Integer quizCount) {
        return collectionService.findAllCollections(collectionSortOption, collectionFields, quizType, quizCount);
    }

    // 북마크한 컬렉션 가져오기
    public List<Collection> findAllByMemberIdAndBookmarked(Long memberId) {
        return collectionService.findAllByMemberIdAndBookmarked(memberId);
    }

    public List<GetQuizzesInCollectionByCollectionField.QuizInCollectionDto> findAllByMemberIdAndCollectionFieldAndBookmarked(Long memberId, CollectionField collectionField) {
        return collectionService.findAllByMemberIdAndCollectionFieldAndBookmarked(memberId, collectionField);
    }

    // 직접 생성한 컬렉션 가져오기
    public GetAllCollectionsResponse findMemberGeneratedCollections(Long memberId) {
        return collectionService.findMemberGeneratedCollections(memberId);
    }

    // 만든 컬렉션 상세
    public GetSingleCollectionResponse findCollectionByCollectionIdAndMemberId(Long collectionId, Long memberId) {
        return collectionService.findCollectionByCollectionIdAndMemberId(collectionId, memberId);
    }

    // 컬렉션 키워드 검색
    public List<Collection> searchCollections(String keyword) {
        return collectionService.searchCollections(keyword);
    }

    @Transactional
    public void deleteCollection(Long collectionId, Long memberId) {
        collectionService.deleteCollection(collectionId, memberId);
    }

    // 컬렉션 정보 수정
    @Transactional
    public void updateCollectionInfo(
            Long collectionId, Long memberId, String name, String description, String emoji, CollectionField collectionField) {
        collectionService.updateCollectionInfo(collectionId, memberId, name, description, emoji, collectionField);
    }

    // 컬렉션에 퀴즈 추가
    @Transactional
    public void addQuizToCollection(Long collectionId, Long memberId, Long quizId) {
        Quiz quiz = quizService.findQuizByQuizIdAndMemberId(quizId, memberId);
        collectionService.addQuizToCollection(collectionId, memberId, quiz);
    }

    // 컬렉션 문제 편집
    @Transactional
    public void updateCollectionQuizzes(
            List<Long> quizIds, Long collectionId, Long memberId) {
        List<Quiz> quizzes = quizService.findAllByMemberIdAndQuizIds(quizIds, memberId);
        if (quizzes.isEmpty()) {
            throw new CustomException(QUIZ_NOT_FOUND_ERROR);
        }
        collectionService.updateCollectionQuizzes(quizzes, collectionId, memberId);
    }

    // 컬렉션 북마크
    @Transactional
    public void createCollectionBookmark(Long memberId, Long collectionId) {
        Member member = memberService.findMemberById(memberId);
        collectionService.createCollectionBookmark(member, collectionId);
    }

    @Transactional
    public void deleteCollectionBookmark(Long memberId, Long collectionId) {
        Member member = memberService.findMemberById(memberId);
        Collection collection = collectionService.findCollectionByCollectionId(collectionId);
        collectionService.deleteCollectionBookmark(member, collection);
    }

    public List<Collection> findInterestFieldCollections(Long memberId) {
        Member member = memberService.findMemberById(memberId);
        List<String> interestCollectionFields = member.getInterestCollectionFields();
        if (interestCollectionFields == null) {
            interestCollectionFields = new ArrayList<>();
        }
        return collectionService.findInterestFieldCollections(interestCollectionFields);
    }

    public GetCollectionSAnalysisResponse findCollectionsAnalysis(Long memberId) {
        return collectionService.findCollectionsAnalysis(memberId);
    }
}
