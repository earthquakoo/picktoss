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
import org.joda.time.Duration;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.function.Function;
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
        return quizSetQuizRepository.findAllQuizzesByQuizSetIdAndMemberId(quizSetId, memberId);
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

    @Transactional
    public List<Quiz> createQuizzes(List<Long> documents, int point, QuizType quizType, Member member) {
        List<Quiz> quizSets = new ArrayList<>();
        List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();

        String quizSetId = createQuizSetId();
        QuizSet quizSet = QuizSet.createQuizSet(quizSetId, member);

        int quizzesPerDocument = point / documents.size();
        int remainingQuizzes = point % documents.size();

        for (Long documentId : documents) {
            List<Quiz> quizzes = quizRepository.findByDocumentIdAndQuizType(documentId, quizType);
            for (int i = 0; i < quizzesPerDocument; i++) {
                Quiz quiz = quizzes.get(i);
                QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
                quizSets.add(quiz);
                quizSetQuizzes.add(quizSetQuiz);
            }
        }
        // 나머지 퀴즈가 있는 경우 해당 문서에 추가 생성
        for (int i = 0; i < remainingQuizzes; i++) {
            List<Quiz> quizzes = quizRepository.findByDocumentIdAndQuizType(documents.get(i), quizType);
            Quiz quiz = quizzes.get(i);
            QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
            quizSets.add(quiz);
            quizSetQuizzes.add(quizSetQuiz);
        }

        quizSetRepository.save(quizSet);
        quizSetQuizRepository.saveAll(quizSetQuizzes);

        return quizSets;
    }

    public List<Quiz> findAllGeneratedQuizzes(Long memberId) {
        return quizSetQuizRepository.findAllQuizzesByMemberId(memberId);
    }

    public List<Quiz> findBookmarkQuiz() {
        return quizRepository.findByBookmark();
    }

    @Transactional
    public void updateQuizLatest(Long documentId) {
        List<Quiz> quizzes = quizRepository.findByDocumentIdAndLatestIs(documentId);

        for (Quiz quiz : quizzes) {
            quiz.updateQuizLatest();
        }
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
    public void updateQuizResult(
            List<GetQuizResultRequest.GetQuizResultQuizDto> quizDtos, String quizSetId, Long memberId) {

        List<QuizSetQuiz> quizSetQuizzes = quizSetQuizRepository.findAllByQuizSetIdAndMemberId(quizSetId, memberId);

        Map<Long, GetQuizResultRequest.GetQuizResultQuizDto> quizDtoMap = quizDtos.stream()
                .collect(Collectors.toMap(GetQuizResultRequest.GetQuizResultQuizDto::getId, Function.identity()));

        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
            Long quizId = quizSetQuiz.getId();

            if (quizDtoMap.containsKey(quizId)) {
                GetQuizResultRequest.GetQuizResultQuizDto quizDto = quizDtoMap.get(quizId);
                Quiz quiz = quizSetQuiz.getQuiz();

                if (!quizDto.isAnswer()) {
                    quiz.addIncorrectAnswerCount();
                }

                quizSetQuiz.updateIsAnswer(quizDto.isAnswer());
                quizSetQuiz.updateElapsedTime(quizDto.getElapsedTime());

                QuizSet quizSet = quizSetQuiz.getQuizSet();
                quizSet.updateSolved();
            }
        }
    }

    public GetQuizAnalysisResponse findQuizAnalysisByCategory(Long memberId, Long categoryId) {
        List<QuizSetQuiz> quizSetQuizzes = quizSetQuizRepository.findAllByMemberIdAndCategoryId(memberId, categoryId);

        int totalQuizCount = quizSetQuizzes.size();
        int mixUpQuizCount = 0;
        int multipleChoiceQuizCount = 0;
        int incorrectAnswerCount = 0;
        LocalTime elapsedTime = LocalTime.of(0, 0, 0);

        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
            Quiz quiz = quizSetQuiz.getQuiz();
            if (quiz.getQuizType() == QuizType.MIX_UP) {
                mixUpQuizCount += 1;
            } else {
                multipleChoiceQuizCount += 1;
            }

            if (!Objects.isNull(quizSetQuiz.getElapsedTime())) {
                elapsedTime = elapsedTime.plusHours(quizSetQuiz.getElapsedTime().getHour())
                        .plusMinutes(quizSetQuiz.getElapsedTime().getMinute())
                        .plusSeconds(quizSetQuiz.getElapsedTime().getSecond());
            }

            incorrectAnswerCount += quiz.getIncorrectAnswerCount();
        }

        return GetQuizAnalysisResponse.builder()
                .totalQuizCount(totalQuizCount)
                .mixUpQuizCount(mixUpQuizCount)
                .multipleQuizCount(multipleChoiceQuizCount)
                .incorrectAnswerCount(incorrectAnswerCount)
                .elapsedTime(elapsedTime)
                .build();
    }

    public List<GetQuizAnswerRateAnalysisResponse.QuizAnswerRateAnalysisDto> findQuizAnswerRateAnalysisByCategory(Long memberId, Long categoryId, int weeks) {
        List<QuizSetQuiz> quizSetQuizzes = quizSetQuizRepository.findAllByMemberIdAndCategoryId(memberId, categoryId);
        HashMap<LocalDate, Integer> incorrectAnswerCountByDate = new HashMap<>();
        HashMap<LocalDate, Integer> totalQuizCountByDate = new HashMap<>();

        LocalDate startDate = LocalDate.now().minusWeeks(weeks);

        for (int i = 0; i <= 7; i++) {
            LocalDate date = startDate.plusDays(i);
            incorrectAnswerCountByDate.put(date, 0);
            totalQuizCountByDate.put(date, 0);
        }

        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
            LocalDate date = quizSetQuiz.getUpdatedAt().toLocalDate();

            if (!date.isBefore(startDate) &&
                    !date.isAfter(startDate.plusDays(7))) {

                totalQuizCountByDate.put(date, totalQuizCountByDate.get(date) + 1);

                if (!Objects.isNull(quizSetQuiz.getIsAnswer()) && !quizSetQuiz.getIsAnswer()) {
                    incorrectAnswerCountByDate.put(date, incorrectAnswerCountByDate.get(date) + 1);
                }
            }
        }

        List<GetQuizAnswerRateAnalysisResponse.QuizAnswerRateAnalysisDto> quizzesDtos = new ArrayList<>();

        for (LocalDate date : incorrectAnswerCountByDate.keySet()) {
            if (!incorrectAnswerCountByDate.isEmpty() && !totalQuizCountByDate.isEmpty()) {
                GetQuizAnswerRateAnalysisResponse.QuizAnswerRateAnalysisDto quizzesDto = GetQuizAnswerRateAnalysisResponse.QuizAnswerRateAnalysisDto.builder()
                        .date(date)
                        .totalQuizCount(totalQuizCountByDate.get(date))
                        .incorrectAnswerCount(incorrectAnswerCountByDate.get(date))
                        .build();

                quizzesDtos.add(quizzesDto);
            }
        }
        return quizzesDtos;
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

    private static String createQuizSetId() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
