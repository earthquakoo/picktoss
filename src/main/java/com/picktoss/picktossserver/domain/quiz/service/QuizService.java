package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.event.entity.Event;
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

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {

    private final QuizRepository quizRepository;
    private final QuizSetRepository quizSetRepository;
    private final QuizSetQuizRepository quizSetQuizRepository;

    public GetQuizSetResponse findQuizSet(String quizSetId, Long memberId) {
        List<QuizSetQuiz> quizSetQuizzes = quizSetQuizRepository.findAllQuizzesByQuizSetIdAndMemberId(quizSetId, memberId);

        if (quizSetQuizzes.isEmpty()) {
            throw new CustomException(ErrorInfo.QUIZ_SET_NOT_FOUND_ERROR);
        }

        QuizSet quizSet = quizSetQuizzes.getFirst().getQuizSet();
        boolean isTodayQuizSet = quizSet.isTodayQuizSet();

        List<GetQuizSetResponse.GetQuizSetQuizDto> quizDtos = new ArrayList<>();
        for (QuizSetQuiz quizzes : quizSetQuizzes) {
            Quiz quiz = quizzes.getQuiz();
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

    public GetQuizSetTodayResponse findQuestionSetToday(Long memberId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStartTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MIN);
        LocalDateTime todayEndTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);

        List<QuizSet> quizSets = quizSetRepository.findByMemberIdAndTodayQuizSetIsOrderByCreatedAt(memberId);
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
        List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();

        String quizSetId = createQuizSetId();
        QuizSet quizSet = QuizSet.createQuizSet(quizSetId, false, member);

        List<Quiz> quizzes = quizRepository.findByQuizTypeAndDocumentIdsIn(quizType, documents);
        if (quizzes.isEmpty()) {
            throw new CustomException(QUIZ_NOT_IN_DOCUMENT);
        }

        // 문서 ID별 퀴즈 개수
        Map<Long, List<Quiz>> documentQuizMap = new HashMap<>();
        for (Quiz quiz : quizzes) {
            Long documentId = quiz.getDocument().getId();
            if (!documentQuizMap.containsKey(documentId)) {
                documentQuizMap.put(documentId, new ArrayList<>());
            }
            documentQuizMap.get(documentId).add(quiz);
        }

        // 총 퀴즈 수
        int totalQuizzes = quizzes.size();

        // 각 문서 ID별 퀴즈 비율
        Map<Long, Double> documentQuizRatioMap = documentQuizMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().size() / (double) totalQuizzes
                ));

        // 각 문서 ID별로 선택할 퀴즈 개수 계산
        Map<Long, Integer> documentQuizCountMap = documentQuizRatioMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (int) Math.floor(entry.getValue() * point)
                ));

        // 현재 선택된 퀴즈 수 계산
        int selectedQuizCount = documentQuizCountMap.values().stream().mapToInt(Integer::intValue).sum();
        int remainingQuizzes = point - selectedQuizCount;

        // 남은 퀴즈를 랜덤하게 배분
        Random random = new Random();
        List<Long> documentIds = new ArrayList<>(documentQuizCountMap.keySet());
        for (int i = 0; i < remainingQuizzes; i++) {
            Long randomDocumentId = documentIds.get(random.nextInt(documentIds.size()));
            documentQuizCountMap.put(randomDocumentId, documentQuizCountMap.get(randomDocumentId) + 1);
        }

        for (Map.Entry<Long, Integer> entry : documentQuizCountMap.entrySet()) {
            Long documentId = entry.getKey();
            int count = entry.getValue();
            List<Quiz> quizList = documentQuizMap.get(documentId);
            Collections.shuffle(quizList);
            for (int i = 0; i < count; i++) {
                Quiz quiz = quizList.get(i);
                QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
                quizSetQuizzes.add(quizSetQuiz);
            }
        }

        quizSetRepository.save(quizSet);
        quizSetQuizRepository.saveAll(quizSetQuizzes);

        return quizSetId;
    }

    public List<Quiz> findAllGeneratedQuizzes(Long documentId, QuizType quizType, Long memberId) {
        return quizRepository.findAllByDocumentIdAndMemberId(documentId, quizType, memberId);
    }

    public List<Quiz> findBookmarkQuiz() {
        return quizRepository.findByBookmark();
    }

    @Transactional
    public void updateQuizLatest(Document document) {
        List<Quiz> quizzes = document.getQuizzes();
        for (Quiz quiz : quizzes) {
            quiz.updateQuizLatestByDocumentReUpload();
        }
    }

    @Transactional
    public void updateBookmarkQuiz(Long quizId, boolean bookmark) {
        Optional<Quiz> optionQuiz = quizRepository.findById(quizId);

        if (optionQuiz.isEmpty()) {
            throw new CustomException(ErrorInfo.QUIZ_NOT_FOUND_ERROR);
        }

        Quiz quiz = optionQuiz.get();

        quiz.updateBookmark(bookmark);
    }

    @Transactional
    public boolean updateQuizResult(
            List<GetQuizResultRequest.GetQuizResultQuizDto> quizDtos, String quizSetId, Long memberId) {
        List<QuizSetQuiz> quizSetQuizzes = quizSetQuizRepository.findAllByQuizSetIdAndMemberId(quizSetId, memberId);

        QuizSet quizSet = quizSetRepository.findByQuizSetIdAndMemberId(quizSetId, memberId)
                .orElseThrow(() -> new CustomException(QUIZ_SET_NOT_FOUND_ERROR));

        Map<Long, GetQuizResultRequest.GetQuizResultQuizDto> quizDtoMap = new HashMap<>();
        for (GetQuizResultRequest.GetQuizResultQuizDto quizDto : quizDtos) {
            quizDtoMap.put(quizDto.getId(), quizDto);
        }

        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
            Quiz quiz = quizSetQuiz.getQuiz();
            if (quizDtoMap.containsKey(quiz.getId())) {
                GetQuizResultRequest.GetQuizResultQuizDto quizDto = quizDtoMap.get(quiz.getId());

                if (!quizDto.isAnswer()) {
                    quiz.addIncorrectAnswerCount();
                }
                quizSetQuiz.updateIsAnswer(quizDto.isAnswer());
                quizSetQuiz.updateElapsedTime(quizDto.getElapsedTime());
            }
        }
        quizSet.updateSolved();

        return quizSet.isTodayQuizSet();
    }

    public GetQuizAnswerRateAnalysisResponse findQuizAnswerRateAnalysisByWeek(Long memberId, Long categoryId, int weeks) {
        List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();
        if (categoryId == 0) {
            quizSetQuizzes = quizSetQuizRepository.findAllByMemberId(memberId);
        } else {
            quizSetQuizzes = quizSetQuizRepository.findAllByMemberIdAndCategoryId(memberId, categoryId);
        }

        HashMap<LocalDate, Integer> incorrectAnswerCountByDate = new LinkedHashMap<>();
        HashMap<LocalDate, Integer> totalQuizCountByDate = new LinkedHashMap<>();

        HashMap<String, Integer> quizAnalysis = quizAnalysis(quizSetQuizzes);
        Integer totalQuizCount = quizAnalysis.get("totalQuizCount");
        Integer mixUpQuizCount = quizAnalysis.get("mixUpQuizCount");
        Integer multipleChoiceQuizCount = quizAnalysis.get("multipleChoiceQuizCount");
        Integer incorrectAnswerCount = quizAnalysis.get("incorrectAnswerCount");
        Integer totalElapsedTimeMs = quizAnalysis.get("totalElapsedTimeMs");

        LocalDate startDate = LocalDate.now().minusWeeks(weeks);

        for (int i = 1; i <= 7; i++) {
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

        HashMap<LocalDate, Integer> incorrectAnswerCountByDate = new LinkedHashMap<>();
        HashMap<LocalDate, Integer> totalQuizCountByDate = new LinkedHashMap<>();

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

    public GetQuizCountByDocumentResponse findQuizCountByDocument(List<Long> documentIds, Long memberId, QuizType type) {
        List<Quiz> quizzes = quizRepository.findByQuizTypeAndDocumentIdsIn(type, documentIds);
        return new GetQuizCountByDocumentResponse(quizzes.size());
    }

    @Transactional
    public void deleteIncorrectQuiz(Long quizId, Long documentId) {
        Quiz quiz = quizRepository.findByQuizIdAndDocumentId(quizId, documentId)
                .orElseThrow(() -> new CustomException(QUIZ_NOT_FOUND_ERROR));

        quizRepository.delete(quiz);
    }

    public boolean checkTodayQuizSet(String quizSetId, Long memberId) {
        QuizSet quizSet = quizSetRepository.findByQuizSetIdAndMemberId(quizSetId, memberId)
                .orElseThrow(() -> new CustomException(QUIZ_SET_NOT_FOUND_ERROR));

        return quizSet.isTodayQuizSet();
    }

    @Transactional
    public void checkContinuousQuizDatesCount(Long memberId, Event event) {
        List<QuizSet> quizSets = quizSetRepository.findByMemberIdAndTodayQuizSetIsOrderByCreatedAt(memberId);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterdayStartTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MIN);
        LocalDateTime yesterdayEndTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);

        List<QuizSet> yesterdayQuizSets = new ArrayList<>();
        for (QuizSet qs : quizSets) {
            if (qs.getCreatedAt().isAfter(yesterdayStartTime.minusDays(1)) && qs.getCreatedAt().isBefore(yesterdayEndTime.minusDays(1))) {
                yesterdayQuizSets.add(qs);
            }
        }

        if (yesterdayQuizSets.isEmpty()) {
            return ;
        }
        QuizSet yesterdayQuizSet = yesterdayQuizSets.stream()
                .sorted(Comparator.comparing(QuizSet::getCreatedAt).reversed())
                .toList()
                .getFirst();

        if (!yesterdayQuizSet.isSolved()) {
            event.initContinuousSolvedQuizDateCount();
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

    // 클라이언트 테스트 전용 API(실제 서비스 사용 X)
    @Transactional
    public String createTodayQuizForTest(Member member) {
        List<Quiz> quizzes = quizRepository.findAllByMemberIdForTest(member.getId());
        Collections.shuffle(quizzes);

        List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();

        String quizSetId = createQuizSetId();
        QuizSet quizSet = QuizSet.createQuizSet(quizSetId, true, member);

        for (int i = 0; i < 9; i++) {
            Quiz quiz = quizzes.get(i);
            quiz.addDeliveredCount();

            QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
            quizSetQuizzes.add(quizSetQuiz);
        }

        quizSetRepository.save(quizSet);
        quizSetQuizRepository.saveAll(quizSetQuizzes);

        return quizSetId;
    }
}
