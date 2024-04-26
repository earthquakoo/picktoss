package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.question.controller.response.GetQuestionSetResponse;
import com.picktoss.picktossserver.domain.question.controller.response.GetQuestionSetTodayResponse;
import com.picktoss.picktossserver.domain.question.entity.Question;
import com.picktoss.picktossserver.domain.question.entity.QuestionQuestionSet;
import com.picktoss.picktossserver.domain.question.entity.QuestionSet;
import com.picktoss.picktossserver.domain.quiz.controller.response.GetQuizSetResponse;
import com.picktoss.picktossserver.domain.quiz.controller.response.GetQuizSetTodayResponse;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final QuizSetRepository quizSetRepository;
    private final QuizRepository quizRepository;

    public List<GetQuizSetResponse.GetQuizSetQuizDto> findQuestionSet(String quizSetId) {
        Optional<QuizSet> optionalQuizSet = quizSetRepository.findById(quizSetId);
        if (optionalQuizSet.isEmpty()) {
            throw new CustomException(QUIZ_SET_NOT_FOUND_ERROR);
        }
        List<QuizSetQuiz> quizSetQuizs = optionalQuizSet.get().getQuizSetQuizzes();
        List<GetQuizSetResponse.GetQuizSetQuizDto> quizDtos = new ArrayList<>();

        for (QuizSetQuiz qqs : quizSetQuizs) {
            Quiz quiz = qqs.getQuiz();
            Document document = quiz.getDocument();
            Category category = document.getCategory();

            GetQuizSetResponse.GetQuizSetCategoryDto categoryDto = GetQuizSetResponse.GetQuizSetCategoryDto.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .build();

            GetQuizSetResponse.GetQuizSetDocumentDto documentDto = GetQuizSetResponse.GetQuizSetDocumentDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .build();

            GetQuizSetResponse.GetQuizSetQuizDto quizDto = GetQuizSetResponse.GetQuizSetQuizDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .document(documentDto)
                    .category(categoryDto)
                    .build();

            quizDtos.add(quizDto);
        }
        return quizDtos;
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

    public void findBookmarkQuiz() {
        List<Quiz> quizzes = quizRepository.findByBookmark();
    }

    @Transactional
    public void updateBookmarkQuiz(Long quizId, boolean bookmark) {

    }
}
