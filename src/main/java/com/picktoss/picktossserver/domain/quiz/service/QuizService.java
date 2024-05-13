package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.event.entity.Event;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.quiz.controller.dto.QuizResponseDto;
import com.picktoss.picktossserver.domain.quiz.controller.mapper.QuizMapper;
import com.picktoss.picktossserver.domain.quiz.controller.request.GetQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.controller.response.*;
import com.picktoss.picktossserver.domain.quiz.entity.Option;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetQuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetRepository;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.global.enums.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizSetRepository quizSetRepository;
    private final QuizSetQuizRepository quizSetQuizRepository;

    public List<Quiz> findQuizSet(String quizSetId, Long memberId) {
        return quizSetQuizRepository.findAllQuizzesByQuizSetId(quizSetId);
    }

    public GetQuizSetTodayResponse findQuestionSetToday(Long memberId, List<Document> documents) {
        if (documents.isEmpty()) {
            return GetQuizSetTodayResponse.builder()
                    .message("Document not create yet.")
                    .build();
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime todayStartTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MIN);
        LocalDateTime todayEndTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);

        List<QuizSet> quizSets = quizSetRepository.findAllByMemberId(memberId);
        List<QuizSet> todayQuizSets = new ArrayList<>();
        for (QuizSet qs : quizSets) {
            if (qs.getCreatedAt().isAfter(todayStartTime) && qs.getCreatedAt().isBefore(todayEndTime)) {
                todayQuizSets.add(qs);
            }
        }

        if (todayQuizSets.isEmpty()) {
            return GetQuizSetTodayResponse.builder()
                    .message("Quiz set not ready.")
                    .build();
        }

        QuizSet todayQuizSet = todayQuizSets.stream()
                .sorted(Comparator.comparing(QuizSet::getCreatedAt).reversed())
                .toList()
                .getFirst();

        return GetQuizSetTodayResponse.builder()
                .quizSetId(todayQuizSet.getId())
                .build();
    }

    public List<Quiz> createQuizzes(List<Long> documents, int point, QuizType quizType, Event event) {
        List<Quiz> quizSets = new ArrayList<>();

        int quizzesPerDocument = point / documents.size();
        int remainingQuizzes = point % documents.size();

        for (Long documentId : documents) {
            List<Quiz> quizzes = quizRepository.findByDocumentIdAndQuizType(documentId, quizType);
            for (int i = 0; i < quizzesPerDocument; i++) {
                Quiz quiz = quizzes.get(i);
                quizSets.add(quiz);
            }
        }
        // 나머지 퀴즈가 있는 경우 해당 문서에 추가 생성
        for (int i = 0; i < remainingQuizzes; i++) {
            List<Quiz> quizzes = quizRepository.findByDocumentIdAndQuizType(documents.get(i), quizType);
            Quiz quiz = quizzes.get(i);
            if (quiz.getQuizType() == quizType) {
                quizSets.add(quiz);
            }
        }

        event.usePoint(point);

        return quizSets;
    }

    public List<Quiz> findAllGeneratedQuizzes(Long memberId) {
        return quizSetQuizRepository.findAllQuizzesByMemberId(memberId);
    }

    public List<Quiz> findBookmarkQuiz() {
        return quizRepository.findByBookmark();
    }

    @Transactional
    public void updateBookmarkQuiz(Long quizId, boolean bookmark) {
        Optional<Quiz> optionQuiz = quizRepository.findById(quizId);

        if (optionQuiz.isEmpty()) {
            return;
        }

        Quiz quiz = optionQuiz.get();

        quiz.updateBookmark(bookmark);
    }

    @Transactional
    public List<GetQuizResultResponse.GetQuizResultCategoryDto> updateQuizResult(List<GetQuizResultRequest.GetQuizResultQuizDto> resultQuizDtos, String quizSetId, Member member) {
        Optional<QuizSet> optionalQuizSet = quizSetRepository.findById(quizSetId);

        if (optionalQuizSet.isEmpty()) {
            throw new CustomException(QUIZ_SET_NOT_FOUND_ERROR);
        }

        QuizSet quizSet = optionalQuizSet.get();

        HashMap<String, Integer> categoryMap = new HashMap<>();

        for (GetQuizResultRequest.GetQuizResultQuizDto resultQuizDto : resultQuizDtos) {
            String categoryName = resultQuizDto.getCategoryName();
            if (!categoryMap.containsKey(categoryName)) {
                categoryMap.put(categoryName, 0);
            }

            System.out.println(categoryMap);
            if (!resultQuizDto.isAnswer()) {
                Long quizId = resultQuizDto.getId();
                Optional<Quiz> optionalQuiz = quizRepository.findById(quizId);

                if (optionalQuiz.isEmpty()) {
                    throw new CustomException(QUIZ_NOT_FOUND_ERROR);
                }

                Quiz quiz = optionalQuiz.get();
                quiz.addIncorrectAnswerCount();

                Integer incorrectAnswerCount = categoryMap.get(categoryName);

                incorrectAnswerCount++;
                categoryMap.put(categoryName, incorrectAnswerCount);
            }
        }

        List<GetQuizResultResponse.GetQuizResultCategoryDto> categoryDtos = new ArrayList<>();

        for (String name : categoryMap.keySet()) {
            GetQuizResultResponse.GetQuizResultCategoryDto categoryDto = GetQuizResultResponse.GetQuizResultCategoryDto.builder()
                    .name(name)
                    .incorrectAnswerCount(categoryMap.get(name))
                    .build();

            categoryDtos.add(categoryDto);
        }

        member.updateContinuousQuizDatesCount(true);
        quizSet.updateSolved();

        return categoryDtos;
    }

    public boolean checkContinuousQuizDatesCount(Long memberId) {
        List<QuizSet> quizSets = quizSetRepository.findAllByMemberId(memberId);

        if (quizSets.isEmpty()) {
            return false;
        }

        QuizSet quizSet = quizSets.stream()
                .sorted(Comparator.comparing(QuizSet::getCreatedAt).reversed())
                .toList()
                .getFirst();

        boolean isWithinOneDay = LocalDateTime.now().minusDays(1).isBefore(quizSet.getCreatedAt());

        if (!quizSet.isSolved()) { // 퀴즈를 풀지 않았을 때 하루가 지났다면 false, 지나지 않았다면 true
            return isWithinOneDay;
        } else { // 퀴즈를 풀었는데 하루가 지났다면 false, 지나지 않았다면 true
            return isWithinOneDay;
        }
    }
}
