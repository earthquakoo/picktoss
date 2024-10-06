package com.picktoss.picktossserver.domain.collection.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.collection.controller.response.GetAllCollectionsResponse;
import com.picktoss.picktossserver.domain.collection.controller.response.GetSingleCollectionResponse;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionBookmark;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuiz;
import com.picktoss.picktossserver.domain.collection.repository.CollectionBookmarkRepository;
import com.picktoss.picktossserver.domain.collection.repository.CollectionQuizRepository;
import com.picktoss.picktossserver.domain.collection.repository.CollectionRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.quiz.entity.Option;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.global.enums.CollectionDomain;
import com.picktoss.picktossserver.global.enums.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.COLLECTION_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final CollectionQuizRepository collectionQuizRepository;
    private final CollectionBookmarkRepository collectionBookmarkRepository;

    @Transactional
    public void createCollection(
            List<Quiz> quizzes, String name, String description, String tag, String emoji, CollectionDomain collectionType, Member member) {

        List<CollectionQuiz> collectionQuizs = new ArrayList<>();

        Collection collection = Collection.createCollection(name, emoji, description, tag, collectionType, member);
        CollectionBookmark collectionBookmark = CollectionBookmark.createCollectionBookmark(collection, member);

        for (Quiz quiz : quizzes) {
            CollectionQuiz collectionQuiz = CollectionQuiz.createQuizCollection(quiz, collection);
            collectionQuizs.add(collectionQuiz);
        }

        collectionRepository.save(collection);
        collectionQuizRepository.saveAll(collectionQuizs);
        collectionBookmarkRepository.save(collectionBookmark);
    }

    // 탐색 컬렉션
    public void findAllCollections(String collectionSortOption, List<CollectionDomain> collectionDomains, QuizType quizType, Integer quizCount) {
        List<GetAllCollectionsResponse.GetAllCollectionsDto> collectionsDtos = new ArrayList<>();
        List<Collection> collections = collectionRepository.findAllOrderByCreatedAtDesc();
        // 파라미터가 null 값이라면 넘어가고 해당 파라미터가 null 값이 아니라면 탐색

        for (Collection collection : collections) {
            int collectionQuizCount = collection.getCollectionQuizzes().size();
            int bookmarkCount = collection.getCollectionBookmarks().size();
            GetAllCollectionsResponse.GetAllCollectionsDto.builder()
                    .id(collection.getId())
                    .name(collection.getName())
                    .emoji(collection.getEmoji())
                    .bookmarkCount(bookmarkCount)
                    .collectionDomain(collection.getCollectionDomain())
                    .memberName(collection.getMember().getName())
                    .quizCount(collectionQuizCount)
                    .build();
        }
    }

    // 내 컬렉션(내가 만든 컬렉션이나 북마크한 컬렉션) 내가 만든 컬렉션은 북마크가 이미 되어있도록 설정(+ 내가 만든 컬렉션은 북마크를 해제할 수 없음)
    public List<Collection> findCollectionByMemberId(Long memberId) {
        return collectionBookmarkRepository.findCollectionByMemberId(memberId);
    }

    // 만든 컬렉션 상세
    public GetSingleCollectionResponse findCollectionByCollectionId(Long collectionId, Long memberId) {
        Collection collection = collectionRepository.findCollectionByIdAndMemberId(collectionId, memberId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        List<GetSingleCollectionResponse.GetSingleCollectionQuizDto> quizzesDtos = new ArrayList<>();

        List<CollectionQuiz> collectionQuizzes = collection.getCollectionQuizzes();
        for (CollectionQuiz collectionQuizz : collectionQuizzes) {
            Quiz quiz = collectionQuizz.getQuiz();
            List<String> optionList = new ArrayList<>();
            if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                List<Option> options = quiz.getOptions();
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
                .name(collection.getName())
                .description(collection.getDescription())
                .tag(collection.getTag())
                .solvedCount(collection.getSolvedCount())
                .bookmarkCount(collection.getCollectionBookmarks().size())
                .quizzes(quizzesDtos)
                .build();
    }

    // 컬렉션 키워드 검색
    public List<Collection> searchCollections(String keyword) {
        return collectionRepository.findByCollectionContaining(keyword);
    }

    // 컬렉션 정보 수정
    @Transactional
    public void updateCollectionInfo(
            Long collectionId, Long memberId, String name, String tag, String description, String emoji, CollectionDomain collectionDomain) {
        Collection collection = collectionRepository.findCollectionByIdAndMemberId(collectionId, memberId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        collection.updateCollectionByUpdateCollectionInfo(name, tag, description, emoji, collectionDomain);
    }

    // 기존에 collection_quiz들을 삭제하고 다시 생성한다?
    @Transactional
    public void updateCollectionQuizzes(List<Quiz> quizzes, Long collectionId, Long memberId) {
        Collection collection = collectionRepository.findCollectionByIdAndMemberId(collectionId, memberId)
                .orElseThrow(() -> new CustomException(COLLECTION_NOT_FOUND));

        List<CollectionQuiz> curCollectionQuizzes = collection.getCollectionQuizzes();
        collectionQuizRepository.deleteAll(curCollectionQuizzes);

        List<CollectionQuiz> newCollectionQuizzes = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            CollectionQuiz collectionQuiz = CollectionQuiz.createQuizCollection(quiz, collection);
            newCollectionQuizzes.add(collectionQuiz);
        }

        collectionQuizRepository.saveAll(newCollectionQuizzes);
    }

}
