package com.picktoss.picktossserver.domain.collection.facade;

import com.picktoss.picktossserver.core.eventlistener.event.s3.S3UploadImagesEvent;
import com.picktoss.picktossserver.core.eventlistener.publisher.s3.S3UploadImagesPublisher;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.collection.controller.response.GetCollectionCategoriesResponse;
import com.picktoss.picktossserver.domain.collection.controller.response.GetCollectionSAnalysisResponse;
import com.picktoss.picktossserver.domain.collection.controller.response.GetQuizzesInCollectionByCollectionCategory;
import com.picktoss.picktossserver.domain.collection.controller.response.GetSingleCollectionResponse;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.service.CollectionService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.service.QuizService;
import com.picktoss.picktossserver.global.enums.collection.CollectionCategory;
import com.picktoss.picktossserver.global.enums.collection.CollectionSortOption;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.QUIZ_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionFacade {

    private final CollectionService collectionService;
    private final QuizService quizService;
    private final MemberService memberService;
    private final S3UploadImagesPublisher s3UploadImagesPublisher;

    @Transactional
    public Long createCollection(
            List<Long> quizIds, String name, String description, String emoji, CollectionCategory collectionType, Long memberId) {
        List<Quiz> quizzes = quizService.findAllByMemberIdAndQuizIds(quizIds, memberId);
        if (quizzes.isEmpty()) {
            throw new CustomException(QUIZ_NOT_FOUND_ERROR);
        }
        Member member = memberService.findMemberById(memberId);
        return collectionService.createCollection(quizzes, name, description, emoji, collectionType, member);
    }

    // 탐색 컬렉션
    public List<Collection> findAllCollections(
            CollectionSortOption collectionSortOption, List<CollectionCategory> collectionCategories, QuizType quizType, Integer quizCount) {
        return collectionService.findAllCollections(collectionSortOption, collectionCategories, quizType, quizCount);
    }

    // 북마크한 컬렉션 가져오기
    public List<Collection> findAllByMemberIdAndBookmarked(Long memberId) {
        return collectionService.findAllByMemberIdAndBookmarked(memberId);
    }

    public List<GetQuizzesInCollectionByCollectionCategory.QuizInCollectionDto> findAllByMemberIdAndCollectionCategoryAndBookmarked(Long memberId, CollectionCategory collectionCategory) {
        return collectionService.findAllByMemberIdAndCollectionCategoryAndBookmarked(memberId, collectionCategory);
    }

    // 직접 생성한 컬렉션 가져오기
    public List<Collection> findMemberGeneratedCollections(Long memberId) {
        return collectionService.findMemberGeneratedCollections(memberId);
    }
    
    // 컬렉션 상세 정보
    public GetSingleCollectionResponse findCollectionInfoByCollectionId(Long collectionId) {
        return collectionService.findCollectionInfoByCollectionId(collectionId);
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
            Long collectionId, Long memberId, String name, String description, String emoji, CollectionCategory collectionCategory) {
        collectionService.updateCollectionInfo(collectionId, memberId, name, description, emoji, collectionCategory);
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

    @Transactional
    public void createCollectionComplaint(List<MultipartFile> files, String content, Long collectionId, Long memberId) {
        Member member = memberService.findMemberById(memberId);

        String s3Key = UUID.randomUUID().toString();
        String s3FolderPath = "picktoss-collection-complaint-images/";

        List<String> s3Keys = new ArrayList<>();
        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();
            String fullS3Key = s3FolderPath + s3Key + "_" + fileName;
            s3Keys.add(fullS3Key);
        }

        s3UploadImagesPublisher.s3UploadImagesPublisher(new S3UploadImagesEvent(files, s3Keys));
        collectionService.createCollectionComplaint(collectionId, content, s3Keys, member);
    }

    public List<Collection> findInterestCategoryCollections(Long memberId) {
        Member member = memberService.findMemberById(memberId);
        List<String> interestCollectionCategories = member.getInterestCollectionCategories();
        if (interestCollectionCategories == null) {
            interestCollectionCategories = new ArrayList<>();
        }
        return collectionService.findInterestCategoryCollections(interestCollectionCategories);
    }

    public List<GetCollectionCategoriesResponse.GetCollectionCategoriesDto> findCollectionCategoriesByMemberId(Long memberId) {
        return collectionService.findCollectionCategoriesByMemberId(memberId);
    }

    public GetCollectionSAnalysisResponse findCollectionsAnalysis(Long memberId) {
        return collectionService.findCollectionsAnalysis(memberId);
    }
}
