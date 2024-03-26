package com.picktoss.picktossserver.domain.question.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.question.controller.response.GetAllCategoryQuestionsResponse;
import com.picktoss.picktossserver.domain.question.controller.response.GetQuestionSetResponse;
import com.picktoss.picktossserver.domain.question.controller.response.GetQuestionSetTodayResponse;
import com.picktoss.picktossserver.domain.question.entity.Question;
import com.picktoss.picktossserver.domain.question.entity.QuestionQuestionSet;
import com.picktoss.picktossserver.domain.question.entity.QuestionSet;
import com.picktoss.picktossserver.domain.question.repository.QuestionRepository;
import com.picktoss.picktossserver.domain.question.repository.QuestionSetRepository;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuestionService {

    private final QuestionSetRepository questionSetRepository;

    public List<GetAllCategoryQuestionsResponse.DocumentDto> findAllCategoryQuestions(List<Document> documents) {
        List<GetAllCategoryQuestionsResponse.DocumentDto> documentDtos = new ArrayList<>();
        List<GetAllCategoryQuestionsResponse.QuestionDto> questionDtos = new ArrayList<>();

        for (Document document : documents) {
            List<Question> questions = document.getQuestions();
            for (Question question : questions) {
                GetAllCategoryQuestionsResponse.QuestionDto questionDto = GetAllCategoryQuestionsResponse.QuestionDto.builder()
                        .id(question.getId())
                        .question(question.getQuestion())
                        .answer(question.getAnswer())
                        .build();

                questionDtos.add(questionDto);
            }

            GetAllCategoryQuestionsResponse.DocumentDto documentDto = GetAllCategoryQuestionsResponse.DocumentDto.builder()
                    .id(document.getId())
                    .documentName(document.getName())
                    .status(document.getStatus())
                    .summary(document.getSummary())
                    .createAt(document.getCreatedAt())
                    .questions(questionDtos)
                    .build();

            documentDtos.add(documentDto);
        }
        return documentDtos;
    }

    public List<GetQuestionSetResponse.QuestionDto> findQuestionSet(String questionSetId) {
        Optional<QuestionSet> questionSet = questionSetRepository.findById(questionSetId);
        if (questionSet.isEmpty()) {
            throw new CustomException(ErrorInfo.QUESTION_SET_NOT_FOUND_ERROR);
        }
        List<QuestionQuestionSet> questionQuestionSets = questionSet.get().getQuestionQuestionSets();
        List<GetQuestionSetResponse.QuestionDto> questionDtos = new ArrayList<>();

        for (QuestionQuestionSet qqs : questionQuestionSets) {
            Question question = qqs.getQuestion();
            Document document = question.getDocument();
            Category category = document.getCategory();

            GetQuestionSetResponse.CategoryDto categoryDto = GetQuestionSetResponse.CategoryDto.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .build();

            GetQuestionSetResponse.DocumentDto documentDto = GetQuestionSetResponse.DocumentDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .build();

            GetQuestionSetResponse.QuestionDto questionDto = GetQuestionSetResponse.QuestionDto.builder()
                    .id(question.getId())
                    .question(question.getQuestion())
                    .answer(question.getAnswer())
                    .document(documentDto)
                    .category(categoryDto)
                    .build();

            questionDtos.add(questionDto);
        }
        return questionDtos;

    }

    public GetQuestionSetTodayResponse findQuestionSetToday(Long memberId, List<Document> documents) {
        if (documents.isEmpty()) {
            return GetQuestionSetTodayResponse.builder()
                    .message("Document not create yet.")
                    .build();
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime todayStartTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MIN);
        LocalDateTime todayEndTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);
        List<QuestionSet> questionSets = questionSetRepository.findAllByMemberId(memberId);
        List<QuestionSet> todayQuestionSets = new ArrayList<>();
        for (QuestionSet qs : questionSets) {
            if (qs.getCreatedAt().isAfter(todayStartTime) && qs.getCreatedAt().isBefore(todayEndTime)) {
                todayQuestionSets.add(qs);
            }
        }

        if (todayQuestionSets.isEmpty()) {
            return GetQuestionSetTodayResponse.builder()
                    .message("Question set not ready.")
                    .build();
        }

        QuestionSet todayQuestionSet = todayQuestionSets.stream()
                .sorted(Comparator.comparing(QuestionSet::getCreatedAt).reversed())
                .toList()
                .getFirst();

        return GetQuestionSetTodayResponse.builder()
                .questionSetId(todayQuestionSet.getId())
                .build();
    }
}
