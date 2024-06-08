package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.quiz.controller.request.GetQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.controller.response.*;
import com.picktoss.picktossserver.domain.quiz.entity.Option;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetQuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetRepository;
import com.picktoss.picktossserver.global.enums.QuizSetResponseType;
import com.picktoss.picktossserver.global.enums.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizSetRepository quizSetRepository;
    private final QuizSetQuizRepository quizSetQuizRepository;

    public GetQuizSetResponse findQuizSet(String quizSetId, Long memberId) {
        List<Quiz> quizzes = quizSetQuizRepository.findAllQuizzesByQuizSetIdAndMemberId(quizSetId, memberId);

        if (quizzes.isEmpty()) {
            throw new CustomException(ErrorInfo.QUIZ_SET_NOT_FOUND_ERROR);
        }

        boolean isTodayQuizSet = quizzes.getFirst().getQuizSetQuizzes().getFirst().getQuizSet().isTodayQuizSet();

        List<GetQuizSetResponse.GetQuizSetQuizDto> quizDtos = new ArrayList<>();
        for (Quiz quiz : quizzes) {

            List<String> optionList = new ArrayList<>();
            if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                List<Option> options = quiz.getOptions();
                for (Option option : options) {
                    optionList.add(option.getOption());
                }
            }

            Document document = quiz.getDocument();

            GetQuizSetResponse.GetQuizSetDocumentDto documentDto = GetQuizSetResponse.GetQuizSetDocumentDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .build();


            Category category = document.getCategory();

            GetQuizSetResponse.GetQuizSetCategoryDto categoryDto = GetQuizSetResponse.GetQuizSetCategoryDto.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .build();

            GetQuizSetResponse.GetQuizSetQuizDto quizDto = GetQuizSetResponse.GetQuizSetQuizDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .options(optionList)
                    .quizType(quiz.getQuizType())
                    .document(documentDto)
                    .category(categoryDto)
                    .build();

            quizDtos.add(quizDto);
        }
        return new GetQuizSetResponse(quizDtos, isTodayQuizSet);
    }

    public GetQuizSetTodayResponse findQuestionSetToday(Long memberId, List<Document> documents) {
        if (documents.isEmpty()) {
            return GetQuizSetTodayResponse.builder()
                    .type(QuizSetResponseType.NOT_READY)
                    .build();
        }
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime todayStartTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MIN);
        LocalDateTime todayEndTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);

        List<QuizSet> quizSets = quizSetRepository.findByMemberIdAndTodayQuizSetIs(memberId);
        List<QuizSet> todayQuizSets = new ArrayList<>();
        for (QuizSet qs : quizSets) {
            if (qs.getCreatedAt().isAfter(todayStartTime) && qs.getCreatedAt().isBefore(todayEndTime)) {
                todayQuizSets.add(qs);
            }
        }

        if (todayQuizSets.isEmpty()) {
            return GetQuizSetTodayResponse.builder()
                    .type(QuizSetResponseType.NOT_READY)
                    .build();
        }

        QuizSet todayQuizSet = todayQuizSets.stream()
                .sorted(Comparator.comparing(QuizSet::getCreatedAt).reversed())
                .toList()
                .getFirst();

        if (todayQuizSet.isSolved()) {
            return GetQuizSetTodayResponse.builder()
                    .quizSetId(todayQuizSet.getId())
                    .type(QuizSetResponseType.DONE)
                    .build();
        }

        return GetQuizSetTodayResponse.builder()
                .quizSetId(todayQuizSet.getId())
                .type(QuizSetResponseType.READY)
                .build();
    }

    @Transactional
    public String createQuizzes(List<Long> documents, int point, QuizType quizType, Member member) {
        List<Quiz> quizSets = new ArrayList<>();
        List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();

        String quizSetId = createQuizSetId();
        QuizSet quizSet = QuizSet.createQuizSet(quizSetId, false, member);

        int quizzesPerDocument = point / documents.size();
        int remainingQuizzes = point % documents.size();

        for (Long documentId : documents) {
            List<Quiz> quizzes = quizRepository.findByDocumentIdAndQuizType(documentId, quizType);
            for (int i = 0; i < quizzesPerDocument; i++) {
                Quiz quiz = quizzes.get(i);
                quiz.addDeliveredCount();

                QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
                quizSets.add(quiz);
                quizSetQuizzes.add(quizSetQuiz);
            }
        }
        // 나머지 퀴즈가 있는 경우 해당 문서에 추가 생성
        for (int i = 0; i < remainingQuizzes; i++) {
            List<Quiz> quizzes = quizRepository.findByDocumentIdAndQuizType(documents.get(i), quizType);
            Quiz quiz = quizzes.get(i);
            quiz.addDeliveredCount();

            QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
            quizSets.add(quiz);
            quizSetQuizzes.add(quizSetQuiz);
        }

        quizSetRepository.save(quizSet);
        quizSetQuizRepository.saveAll(quizSetQuizzes);

        return quizSetId;
    }

    public List<Quiz> findAllGeneratedQuizzes(Long documentId, Long memberId) {
        return quizRepository.findAllByDocumentIdAndMemberId(documentId, memberId);
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
    public boolean updateQuizResult(
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
        boolean isTodayQuizSet = quizSetQuizzes.getFirst().getQuizSet().isTodayQuizSet();

        return isTodayQuizSet;
    }

    public GetQuizAnswerRateAnalysisResponse findQuizAnswerRateAnalysisByWeek(Long memberId, Long categoryId, int weeks) {
        List<QuizSetQuiz> quizSetQuizzes;
        if (categoryId == 0) {
            quizSetQuizzes = quizSetQuizRepository.findAllByMemberId(memberId);
        } else {
            quizSetQuizzes = quizSetQuizRepository.findAllByMemberIdAndCategoryId(memberId, categoryId);
        }

        HashMap<LocalDate, Integer> incorrectAnswerCountByDate = new HashMap<>();
        HashMap<LocalDate, Integer> totalQuizCountByDate = new HashMap<>();

        HashMap<String, Integer> quizAnalysis = quizAnalysis(quizSetQuizzes);
        Integer totalQuizCount = quizAnalysis.get("totalQuizCount");
        Integer mixUpQuizCount = quizAnalysis.get("mixUpQuizCount");
        Integer multipleChoiceQuizCount = quizAnalysis.get("multipleChoiceQuizCount");
        Integer incorrectAnswerCount = quizAnalysis.get("incorrectAnswerCount");
        Integer totalElapsedTimeMs = quizAnalysis.get("totalElapsedTimeMs");

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
        return new GetQuizAnswerRateAnalysisResponse(
                totalQuizCount,
                mixUpQuizCount,
                multipleChoiceQuizCount,
                incorrectAnswerCount,
                totalElapsedTimeMs,
                quizzesDtos
                );
    }

    public GetQuizAnswerRateAnalysisResponse findQuizAnswerRateAnalysisByMonth(Long memberId, Long categoryId, int year, int month) {
        List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();
        if (categoryId == 0) {
            quizSetQuizzes = quizSetQuizRepository.findAllByMemberId(memberId);
        } else {
            quizSetQuizzes = quizSetQuizRepository.findAllByMemberIdAndCategoryId(memberId, categoryId);
        }

        HashMap<LocalDate, Integer> incorrectAnswerCountByDate = new HashMap<>();
        HashMap<LocalDate, Integer> totalQuizCountByDate = new HashMap<>();

        HashMap<String, Integer> quizAnalysis = quizAnalysis(quizSetQuizzes);
        Integer totalQuizCount = quizAnalysis.get("totalQuizCount");
        Integer mixUpQuizCount = quizAnalysis.get("mixUpQuizCount");
        Integer multipleChoiceQuizCount = quizAnalysis.get("multipleChoiceQuizCount");
        Integer incorrectAnswerCount = quizAnalysis.get("incorrectAnswerCount");
        Integer totalElapsedTimeMs = quizAnalysis.get("totalElapsedTimeMs");

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            incorrectAnswerCountByDate.put(date, 0);
            totalQuizCountByDate.put(date, 0);
        }

        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
            LocalDate date = quizSetQuiz.getUpdatedAt().toLocalDate();

            if (!date.isBefore(startDate) && !date.isAfter(endDate)) {
                totalQuizCountByDate.put(date, totalQuizCountByDate.get(date) + 1);

                if (!Objects.isNull(quizSetQuiz.getIsAnswer()) && !quizSetQuiz.getIsAnswer()) {
                    incorrectAnswerCountByDate.put(date, incorrectAnswerCountByDate.get(date) + 1);
                }
            }
        }

        List<GetQuizAnswerRateAnalysisResponse.QuizAnswerRateAnalysisDto> quizzesDtos = new ArrayList<>();

        for (LocalDate date : incorrectAnswerCountByDate.keySet()) {
            GetQuizAnswerRateAnalysisResponse.QuizAnswerRateAnalysisDto quizzesDto = GetQuizAnswerRateAnalysisResponse.QuizAnswerRateAnalysisDto.builder()
                    .date(date)
                    .totalQuizCount(totalQuizCountByDate.get(date))
                    .incorrectAnswerCount(incorrectAnswerCountByDate.get(date))
                    .build();

            quizzesDtos.add(quizzesDto);
        }

        return new GetQuizAnswerRateAnalysisResponse(
                totalQuizCount,
                mixUpQuizCount,
                multipleChoiceQuizCount,
                incorrectAnswerCount,
                totalElapsedTimeMs,
                quizzesDtos
        );
    }

    public boolean checkContinuousQuizDatesCount(Long memberId) {
        List<QuizSet> quizSets = quizSetRepository.findByMemberIdAndTodayQuizSetIs(memberId);

        if (quizSets.isEmpty()) {
            return true;
        }

        QuizSet quizSet = quizSets.stream()
                .sorted(Comparator.comparing(QuizSet::getCreatedAt).reversed())
                .toList()
                .getFirst();

        boolean isWithinOneDay = LocalDateTime.now().minusDays(1).isBefore(quizSet.getCreatedAt());

        if (isWithinOneDay) { // 생성된 퀴즈셋이 하루 이내라면 true
            return true;
        } else { // 생성된 퀴즈셋이 하루가 지났고 퀴즈를 풀었다면 true, 풀지 않았다면 false
            return quizSet.isSolved();
        }
    }

    private static String createQuizSetId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private HashMap<String, Integer> quizAnalysis(List<QuizSetQuiz> quizSetQuizzes) {
        HashMap<String, Integer> quizAnalysis = new HashMap<>();
        quizAnalysis.put("totalQuizCount", quizSetQuizzes.size());
        quizAnalysis.put("mixUpQuizCount", 0);
        quizAnalysis.put("multipleChoiceQuizCount", 0);
        quizAnalysis.put("incorrectAnswerCount", 0);
        quizAnalysis.put("totalElapsedTimeMs", 0);

        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
            Quiz quiz = quizSetQuiz.getQuiz();
            if (quiz.getQuizType() == QuizType.MIX_UP) {
                quizAnalysis.put("mixUpQuizCount", quizAnalysis.get("mixUpQuizCount") + 1);
            } else {
                quizAnalysis.put("multipleChoiceQuizCount", quizAnalysis.get("multipleChoiceQuizCount") + 1);
            }

            if (!Objects.isNull(quizSetQuiz.getElapsedTimeMs())) {
                int elapsedTimeMs = quizSetQuiz.getElapsedTimeMs();
                quizAnalysis.put("totalElapsedTimeMs", quizAnalysis.get("totalElapsedTimeMs") + elapsedTimeMs);
            }
            quizAnalysis.put("incorrectAnswerCount", quizAnalysis.get("incorrectAnswerCount") + quiz.getIncorrectAnswerCount());
        }
        return quizAnalysis;
    }
}
