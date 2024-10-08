package com.picktoss.picktossserver.domain.collection.facade;

import com.picktoss.picktossserver.domain.collection.controller.response.GetAllCollectionsResponse;
import com.picktoss.picktossserver.domain.collection.controller.response.GetSingleCollectionResponse;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.service.CollectionService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.service.QuizService;
import com.picktoss.picktossserver.global.enums.CollectionDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionFacade {

    private final CollectionService collectionService;
    private final QuizService quizService;
    private final MemberService memberService;

    @Transactional
    public void createCollection(
            List<Long> quizIds, String name, String description, String tag, String emoji, CollectionDomain collectionType, Long memberId) {
        List<Quiz> quizzes = quizService.findQuizzesByQuizIds(quizIds, memberId);
        Long id = quizzes.getFirst().getId();
        System.out.println("quiz id = " + id);
        Member member = memberService.findMemberById(memberId);
        collectionService.createCollection(quizzes, name, description, tag, emoji, collectionType, member);
    }

    // 탐색 컬렉션
    public List<GetAllCollectionsResponse.GetAllCollectionsDto> findAllCollections(
            String collectionSortOption, List<String> collectionDomains, String quizType, Integer quizCount) {
        return collectionService.findAllCollections(collectionSortOption, collectionDomains, quizType, quizCount);
    }

    // 내 컬렉션(내가 만든 컬렉션이나 북마크한 컬렉션) 내가 만든 컬렉션은 북마크가 이미 되어있도록 설정(+ 내가 만든 컬렉션은 북마크를 해제할 수 없음)
    public List<Collection> findCollectionByMemberId(Long memberId) {
        return collectionService.findCollectionByMemberId(memberId);
    }

    // 만든 컬렉션 상세
    public GetSingleCollectionResponse findCollectionByCollectionId(Long collectionId, Long memberId) {
        return collectionService.findCollectionByCollectionId(collectionId, memberId);
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
            Long collectionId, Long memberId, String name, String tag, String description, String emoji, CollectionDomain collectionDomain) {
        collectionService.updateCollectionInfo(collectionId, memberId, name, tag, description, emoji, collectionDomain);
    }

    // 컬렉션 문제 편집
    @Transactional
    public void updateCollectionQuizzes(
            List<Long> quizIds, Long collectionId, Long memberId) {
        List<Quiz> quizzes = quizService.findQuizzesByQuizIds(quizIds, memberId);
        collectionService.updateCollectionQuizzes(quizzes, collectionId, memberId);
    }
}
