package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.core.event.event.EmailSenderEvent;
import com.picktoss.picktossserver.core.event.publisher.EmailSenderPublisher;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionSolvedRecord;
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
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
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
    private final JdbcTemplate jdbcTemplate;
    private final EmailSenderPublisher emailSenderPublisher;

    private static final String exampleQuizzes = "defaultQuiz/example_quiz_set.json";

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

    public GetExampleQuizSetResponse findExampleQuizSet() {
        List<GetExampleQuizSetResponse.CreateExampleQuizDto> quizzes = new ArrayList<>();

        try {
            ClassPathResource classPathResource = new ClassPathResource(exampleQuizzes);
            InputStream inputStream = classPathResource.getInputStream();

            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            String jsonString = stringBuilder.toString();
            JSONObject jsonObject = new JSONObject(jsonString);

            JSONArray jArray = jsonObject.getJSONArray("quizzes");
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject obj = jArray.getJSONObject(i);
                String question = obj.getString("question");
                String answer = obj.getString("answer");
                String explanation = obj.getString("explanation");
                QuizType quizType = QuizType.valueOf(obj.getString("quiz_type"));

                JSONArray optionsArray = obj.getJSONArray("options");
                List<String> options = new ArrayList<>();
                for (int j = 0; j < optionsArray.length(); j++) {
                    String option = optionsArray.getString(j);
                    options.add(option);
                }
                GetExampleQuizSetResponse.CreateExampleQuizDto quizDto = GetExampleQuizSetResponse.CreateExampleQuizDto.builder()
                        .question(question)
                        .answer(answer)
                        .explanation(explanation)
                        .quizType(quizType)
                        .options(options)
                        .build();

                quizzes.add(quizDto);
            }

        } catch (IOException e) {
            throw new CustomException(DEFAULT_FILE_NOT_FOUND);
        }

        return new GetExampleQuizSetResponse(quizzes);
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
        Set<Quiz> quizzes = document.getQuizzes();
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
                quizSetQuiz.updateChoseAnswer(quizDto.getChoseAnswer());
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

        LocalDate startDate = LocalDate.now().minusWeeks(weeks).plusDays(1);

        for (int i = 0; i <= 6; i++) {
            LocalDate date = startDate.plusDays(i);
            incorrectAnswerCountByDate.put(date, 0);
            totalQuizCountByDate.put(date, 0);
        }

        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
            LocalDate date = quizSetQuiz.getUpdatedAt().toLocalDate();

            if (!date.isBefore(startDate) && !date.isAfter(startDate.plusDays(7))) {
                totalQuizCountByDate.put(date, totalQuizCountByDate.getOrDefault(date, 0) + 1);

                if (!Objects.isNull(quizSetQuiz.getIsAnswer()) && !quizSetQuiz.getIsAnswer()) {
                    incorrectAnswerCountByDate.put(date, incorrectAnswerCountByDate.getOrDefault(date, 0) + 1);
                }
            }
        }

        List<GetQuizAnswerRateAnalysisResponse.QuizAnswerRateAnalysisDto> quizzesDtos = new ArrayList<>();

        for (LocalDate date : incorrectAnswerCountByDate.keySet()) {
            if (!incorrectAnswerCountByDate.isEmpty() && !totalQuizCountByDate.isEmpty()) {
                GetQuizAnswerRateAnalysisResponse.QuizAnswerRateAnalysisDto quizzesDto = GetQuizAnswerRateAnalysisResponse.QuizAnswerRateAnalysisDto.builder()
                        .date(date)
                        .totalQuizCount(totalQuizCountByDate.getOrDefault(date, 0))
                        .incorrectAnswerCount(incorrectAnswerCountByDate.getOrDefault(date, 0))
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
                totalQuizCountByDate.put(date, totalQuizCountByDate.getOrDefault(date, 0) + 1);

                if (!Objects.isNull(quizSetQuiz.getIsAnswer()) && !quizSetQuiz.getIsAnswer()) {
                    incorrectAnswerCountByDate.put(date, incorrectAnswerCountByDate.getOrDefault(date, 0) + 1);
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
        LocalDateTime todayStartTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MIN);
        LocalDateTime todayEndTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);

        List<QuizSet> todayQuizSets = new ArrayList<>();
        List<QuizSet> yesterdayQuizSets = new ArrayList<>();
        for (QuizSet qs : quizSets) {
            if (qs.getCreatedAt().isAfter(todayStartTime) && qs.getCreatedAt().isBefore(todayEndTime)) {
                todayQuizSets.add(qs);
            }

            if (qs.getCreatedAt().isAfter(todayStartTime.minusDays(1)) && qs.getCreatedAt().isBefore(todayEndTime.minusDays(1))) {
                yesterdayQuizSets.add(qs);
            }
        }

        if (todayQuizSets.isEmpty()) {
            event.initContinuousSolvedQuizDateCount();
            return ;
        }

        QuizSet todayQuizSet = todayQuizSets.stream()
                .sorted(Comparator.comparing(QuizSet::getCreatedAt).reversed())
                .toList()
                .getFirst();

        if (yesterdayQuizSets.isEmpty()) {
            return ;
        }

        QuizSet yesterdayQuizSet = yesterdayQuizSets.stream()
                .sorted(Comparator.comparing(QuizSet::getCreatedAt).reversed())
                .toList()
                .getFirst();

        if (!yesterdayQuizSet.isSolved()) {
            if (!todayQuizSet.isSolved()) {
                event.initContinuousSolvedQuizDateCount();
            }
        }
    }

    /**
     * Picktoss update
     */

    @Transactional
    public String createQuizzesByDocument(Long documentId, Member member, QuizType quizType, Integer quizCount) {
        List<Quiz> quizzes = quizRepository.findByDocumentIdAndMemberIdAndIsQuizLatest(documentId, member.getId());

        if (quizType != null) {
            quizzes = quizzes.stream()
                    .filter(quiz -> quiz.getQuizType() == quizType)
                    .toList();
        }

        if (quizCount > quizzes.size()) {
            throw new CustomException(QUIZ_COUNT_EXCEEDED);
        }

        Collections.shuffle(quizzes);
        quizzes = quizzes.subList(0, quizCount + 1);

        List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();
        String quizSetId = createQuizSetId();
        QuizSet quizSet = QuizSet.createQuizSet(quizSetId, false, member);

        for (Quiz quiz : quizzes) {
            QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
            quizSetQuizzes.add(quizSetQuiz);
        }

        quizSetRepository.save(quizSet);
        quizSetQuizRepository.saveAll(quizSetQuizzes);

        return quizSetId;
    }

    public List<GetQuizRecordResponse.GetQuizRecordDto> findAllQuizRecord(Member member, List<CollectionSolvedRecord> collectionSolvedRecords) {
        List<QuizSet> quizSets = quizSetRepository.findQuizSetsWithQuizAndQuizSetQuizByMemberId(member.getId());

        List<GetQuizRecordResponse.GetQuizRecordDto> quizRecordDtos = new ArrayList<>();

        int continuousQuizDatesCount = 0;
        for (QuizSet quizSet : quizSets) {
            int quizCount = quizSet.getQuizSetQuizzes().size();
            int score = quizCount;
            List<QuizSetQuiz> quizSetQuizzes = quizSet.getQuizSetQuizzes();
            for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
                if (!quizSetQuiz.getIsAnswer()) {
                    score -= 1;
                }
            }
            Quiz quiz = quizSet.getQuizSetQuizzes().getFirst().getQuiz();
            String recordName = quiz.getDocument().getName();

            if (quizSet.isTodayQuizSet()) {
                if (quizSet.isSolved()) {
                    continuousQuizDatesCount += 1;
                } else {
                    continuousQuizDatesCount = 0;
                }
                recordName = "오늘의 퀴즈";
            }

            GetQuizRecordResponse.GetQuizRecordDto recordDto = GetQuizRecordResponse.GetQuizRecordDto.builder()
                    .quizSetId(quizSet.getId())
                    .name(recordName)
                    .quizCount(quizCount)
                    .score(score)
                    .solvedDate(quizSet.getCreatedAt())
                    .continuousQuizDatesCount(continuousQuizDatesCount)
                    .build();

            quizRecordDtos.add(recordDto);
        }

        for (CollectionSolvedRecord collectionSolvedRecord : collectionSolvedRecords) {
            Collection collection = collectionSolvedRecord.getCollection();

            GetQuizRecordResponse.GetQuizRecordDto recordDto = GetQuizRecordResponse.GetQuizRecordDto.builder()
                    .collectionId(collection.getId())
                    .name(collection.getName())
                    .quizCount(collection.getCollectionQuizzes().size())
                    .score(collectionSolvedRecord.getScore())
                    .solvedDate(collectionSolvedRecord.getCreatedAt())
                    .continuousQuizDatesCount(continuousQuizDatesCount)
                    .build();

            quizRecordDtos.add(recordDto);
        }
        return quizRecordDtos;
    }

    // 해결한 컬렉션에 대한 상세 기록
    public GetSingleQuizSetRecordResponse findQuizSetRecordByMemberIdAndQuizSetId(Long memberId, String quizSetId) {
        QuizSet quizSet = quizSetRepository.findQuizSetWithQuizSetQuizAndQuizAndDocumentAndCategoryByMemberIdAndQuizSetId(memberId, quizSetId)
                .orElseThrow(() -> new CustomException(QUIZ_SET_NOT_FOUND_ERROR));

        List<GetSingleQuizSetRecordResponse.GetSingleQuizSetRecordDto> quizSetRecordDtos = new ArrayList<>();

        int elapsedTimeMs = 0;
        List<QuizSetQuiz> quizSetQuizzes = quizSet.getQuizSetQuizzes();
        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
            elapsedTimeMs += quizSetQuiz.getElapsedTimeMs();
            Document document = quizSetQuiz.getQuiz().getDocument();
            Category category = document.getCategory();

            GetSingleQuizSetRecordResponse.GetSingleQuizSetRecordDto quizSetRecordDto = GetSingleQuizSetRecordResponse.GetSingleQuizSetRecordDto.builder()
                    .question(quizSetQuiz.getQuiz().getQuestion())
                    .answer(quizSetQuiz.getQuiz().getAnswer())
                    .explanation(quizSetQuiz.getQuiz().getExplanation())
                    .choseAnswer(quizSetQuiz.getChoseAnswer())
                    .isAnswer(quizSetQuiz.getIsAnswer())
                    .documentName(document.getName())
                    .categoryName(category.getName())
                    .build();

            quizSetRecordDtos.add(quizSetRecordDto);
        }

        return new GetSingleQuizSetRecordResponse(quizSet.getCreatedAt(), elapsedTimeMs, quizSetRecordDtos);
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
            if (!quizSetQuiz.getIsAnswer()) {
                quizAnalysis.put("incorrectAnswerCount", quizAnalysis.get("incorrectAnswerCount") + 1);
            }
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

    @Transactional
    public void quizCreate(List<Quiz> quizzes, List<QuizSet> quizSets, List<QuizSetQuiz> quizSetQuizzes, Member member) {
//        List<QuizSet> quizSets = new ArrayList<>();
//        List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();
//        List<Quiz> quizzesBySortedDeliveredCount = new ArrayList<>();
//        List<Category> categories = member.getCategories();
//        for (Category category : categories) {
//            if (category.getDocuments() == null) {
//                continue;
//            }
//            Set<Document> documents = category.getDocuments();
//            for (Document document : documents) {
//                if (document.getQuizzes() == null) {
//                    continue;
//                }
//                Set<Quiz> quizzes = document.getQuizzes();
//                if (quizzes.isEmpty()) {
//                    continue;
//                }
//                // quiz.deliveredCount 순으로 정렬 or List로 정렬
//                List<Quiz> quizList = quizzes.stream().sorted((e1, e2) -> e1.getDeliveredCount()).limit(10).toList();
//                quizzesBySortedDeliveredCount.addAll(quizList);
//            }
//        }
//        String quizSetId = UUID.randomUUID().toString().replace("-", "");
//        QuizSet quizSet = QuizSet.createQuizSet(quizSetId, true, member);
//        quizSets.add(quizSet);
//
//        quizzesBySortedDeliveredCount.stream().sorted((e1, e2) -> e1.getDeliveredCount());
//        int quizCount = 0;
//
//        for (Quiz quiz : quizzesBySortedDeliveredCount) {
//            QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
//            quizSetQuizzes.add(quizSetQuiz);
//            quiz.addDeliveredCount();
//            quizCount += 1;
//            if (quizCount == 10) {
//                break;
//            }
//        }
//
//        quizSetRepository.saveAll(quizSets);
//        quizSetQuizRepository.saveAll(quizSetQuizzes);
        String insertQuizSetSql = "INSERT INTO quiz_set (id, solved, is_today_quiz_set, member_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(
                insertQuizSetSql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        QuizSet quizSet = quizSets.get(i);
                        ps.setString(1, quizSet.getId());
                        ps.setBoolean(2, quizSet.isSolved());
                        ps.setBoolean(3, quizSet.isTodayQuizSet());
                        ps.setLong(4, quizSet.getMember().getId());
                        ps.setObject(5, LocalDateTime.now());
                        ps.setObject(6, LocalDateTime.now());
                    }

                    @Override
                    public int getBatchSize() {
                        return quizSets.size();
                    }
                }
        );

        String insertQuizSetQuizzesSql = "INSERT INTO quiz_set_quiz (quiz_id, quiz_set_id, created_at, updated_at) VALUES (?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(
                insertQuizSetQuizzesSql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        QuizSetQuiz quizSetQuiz = quizSetQuizzes.get(i);
                        ps.setObject(1, quizSetQuiz.getQuiz().getId());
                        ps.setObject(2, quizSetQuiz.getQuizSet().getId());
                        ps.setObject(3, LocalDateTime.now());
                        ps.setObject(4, LocalDateTime.now());
                    }

                    @Override
                    public int getBatchSize() {
                        return quizSetQuizzes.size();
                    }
                }
        );

        String updateQuizSql = "UPDATE quiz SET delivered_count = delivered_count + 1 WHERE id = ?";
        jdbcTemplate.batchUpdate(
                updateQuizSql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Quiz quiz = quizzes.get(i);
                        ps.setObject(1, quiz.getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return quizzes.size();
                    }
                }
        );
    }

    @Transactional
    public void quizChunkBatchInsert(
            List<Quiz> quizzes, List<QuizSet> quizSets, List<QuizSetQuiz> quizSetQuizzes, List<Member> members) {
        String insertQuizSetSql = "INSERT INTO quiz_set (id, solved, is_today_quiz_set, member_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(
                insertQuizSetSql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        QuizSet quizSet = quizSets.get(i);
                        ps.setString(1, quizSet.getId());
                        ps.setBoolean(2, quizSet.isSolved());
                        ps.setBoolean(3, quizSet.isTodayQuizSet());
                        ps.setLong(4, quizSet.getMember().getId());
                        ps.setObject(5, LocalDateTime.now());
                        ps.setObject(6, LocalDateTime.now());
                    }

                    @Override
                    public int getBatchSize() {
                        return quizSets.size();
                    }
                }
        );

        String insertQuizSetQuizzesSql = "INSERT INTO quiz_set_quiz (quiz_id, quiz_set_id, created_at, updated_at) VALUES (?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(
                insertQuizSetQuizzesSql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        QuizSetQuiz quizSetQuiz = quizSetQuizzes.get(i);
                        ps.setObject(1, quizSetQuiz.getQuiz().getId());
                        ps.setObject(2, quizSetQuiz.getQuizSet().getId());
                        ps.setObject(3, LocalDateTime.now());
                        ps.setObject(4, LocalDateTime.now());
                    }

                    @Override
                    public int getBatchSize() {
                        return quizSetQuizzes.size();
                    }
                }
        );

        String updateQuizSql = "UPDATE quiz SET delivered_count = delivered_count + 1 WHERE id = ?";
        jdbcTemplate.batchUpdate(
                updateQuizSql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        Quiz quiz = quizzes.get(i);
                        ps.setObject(1, quiz.getId());
                    }

                    @Override
                    public int getBatchSize() {
                        return quizzes.size();
                    }
                }
        );

        emailSenderPublisher.emailSenderPublisher(new EmailSenderEvent(members));
    }

    public List<Quiz> findQuizzesByQuizIds(List<Long> quizIds, Long memberId) {
        return quizRepository.findQuizzesByQuizIds(memberId, quizIds);
    }
}
