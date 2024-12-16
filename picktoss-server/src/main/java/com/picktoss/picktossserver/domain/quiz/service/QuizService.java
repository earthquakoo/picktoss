package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.core.eventlistener.event.email.EmailSenderEvent;
import com.picktoss.picktossserver.core.eventlistener.publisher.email.EmailSenderPublisher;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.collection.entity.Collection;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuiz;
import com.picktoss.picktossserver.domain.directory.entity.Directory;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.quiz.controller.request.UpdateQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.controller.request.UpdateRandomQuizResultRequest;
import com.picktoss.picktossserver.domain.quiz.controller.response.*;
import com.picktoss.picktossserver.domain.quiz.entity.Option;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetQuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetRepository;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetResponseType;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.*;

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

    public GetQuizSetResponse findQuizSetByQuizSetIdAndQuizSetType(String quizSetId, QuizSetType quizSetType, Long memberId) {
        List<QuizSetQuiz> quizSetQuizzes = quizSetQuizRepository.findAllByQuizSetIdAndMemberId(quizSetId, memberId);
        QuizSet quizSet = quizSetQuizzes.getFirst().getQuizSet();
        if (quizSet.getQuizSetType() != quizSetType) {
            throw new CustomException(QUIZ_SET_TYPE_ERROR);
        }

        List<GetQuizSetResponse.GetQuizSetQuizDto> quizDtos = new ArrayList<>();
        for (QuizSetQuiz quizzes : quizSetQuizzes) {
            Quiz quiz = quizzes.getQuiz();
            List<String> optionList = new ArrayList<>();
            if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                Set<Option> options = quiz.getOptions();
                for (Option option : options) {
                    optionList.add(option.getOption());
                }
            }

            Document document = quiz.getDocument();

            GetQuizSetResponse.GetQuizSetDocumentDto documentDto = GetQuizSetResponse.GetQuizSetDocumentDto.builder()
                    .id(document.getId())
                    .name(document.getName())
                    .build();


            Directory directory = document.getDirectory();

            GetQuizSetResponse.GetQuizSetDirectoryDto directoryDto = GetQuizSetResponse.GetQuizSetDirectoryDto.builder()
                    .id(directory.getId())
                    .name(directory.getName())
                    .build();

            GetQuizSetResponse.GetQuizSetQuizDto quizDto = GetQuizSetResponse.GetQuizSetQuizDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .options(optionList)
                    .quizType(quiz.getQuizType())
                    .document(documentDto)
                    .directory(directoryDto)
                    .build();

            quizDtos.add(quizDto);
        }
        return new GetQuizSetResponse(quizDtos);
    }

    public GetQuizSetTodayResponse findQuestionSetToday(Long memberId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStartTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MIN);
        LocalDateTime todayEndTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);

        List<QuizSet> quizSets = quizSetRepository.findByMemberIdAndTodayQuizSetOrderByCreatedAtDesc(memberId, QuizSetType.TODAY_QUIZ_SET);
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
                .quizSetType(QuizSetType.TODAY_QUIZ_SET)
                .type(QuizSetResponseType.READY)
                .createdAt(todayQuizSet.getCreatedAt())
                .build();
    }

    public List<Quiz> findAllByMemberIdAndDirectoryId(Long memberId, Long directoryId) {
        List<Quiz> quizzes = quizRepository.findAllByMemberIdAndDirectoryId(memberId, directoryId);
        Collections.shuffle(quizzes);
        return quizzes;
    }

    public List<Quiz> findAllGeneratedQuizzesByDocumentId(Long documentId, QuizType quizType, Long memberId) {
        if (quizType != null) {
            return quizRepository.findAllByDocumentIdAndQuizTypeAndMemberId(documentId, quizType, memberId);
        }
        return quizRepository.findAllByDocumentIdAndMemberId(documentId, memberId);
    }

    public GetDocumentsNeedingReviewPickResponse findDocumentsNeedingReviewPick(Long memberId, Long documentId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<QuizSetQuiz> quizSetQuizzes = quizSetQuizRepository.findByMemberIdAndDocumentIdAndSolvedTrueAndCreatedAtAfter(memberId, documentId, sevenDaysAgo);

        Map<Quiz, QuizSetQuiz> quizMap = new HashMap<>();
        // 중복된 퀴즈 제거 후 map으로 변경
        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
            Quiz quiz = quizSetQuiz.getQuiz();
            quizMap.putIfAbsent(quiz, quizSetQuiz);
        }

        List<GetDocumentsNeedingReviewPickResponse.GetReviewQuizDto> quizDtos = new ArrayList<>();
        for (Quiz quiz : quizMap.keySet()) {
            QuizSetQuiz quizSetQuiz = quizMap.get(quiz);
            String description = "";
            if (quizSetQuiz.getElapsedTimeMs() >= 20000) {
                description = "20초 이상 소요";
            }

            if (!quizSetQuiz.getIsAnswer()) {
                description = "오답";
            }
            List<String> optionList = new ArrayList<>();
            if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                Set<Option> options = quiz.getOptions();
                if (options.isEmpty()) {
                    continue;
                }
                for (Option option : options) {
                    optionList.add(option.getOption());
                }
            }

            GetDocumentsNeedingReviewPickResponse.GetReviewQuizDto quizDto = GetDocumentsNeedingReviewPickResponse.GetReviewQuizDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .options(optionList)
                    .quizType(quiz.getQuizType())
                    .description(description)
                    .build();

            quizDtos.add(quizDto);
        }

        return new GetDocumentsNeedingReviewPickResponse(quizDtos);
    }

    @Transactional
    public QuizSetType updateQuizResult(
            List<UpdateQuizResultRequest.UpdateQuizResultQuizDto> quizDtos, String quizSetId, Long memberId) {
        QuizSet quizSet = quizSetRepository.findQuizSetByMemberIdAndQuizSetId(memberId, quizSetId)
                .orElseThrow(() -> new CustomException(QUIZ_SET_NOT_FOUND_ERROR));

        List<QuizSetQuiz> quizSetQuizzes = quizSet.getQuizSetQuizzes();

        Map<Long, UpdateQuizResultRequest.UpdateQuizResultQuizDto> quizDtoMap = new HashMap<>();
        for (UpdateQuizResultRequest.UpdateQuizResultQuizDto quizDto : quizDtos) {
            quizDtoMap.put(quizDto.getId(), quizDto);
        }

        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
            Quiz quiz = quizSetQuiz.getQuiz();
            if (quizDtoMap.containsKey(quiz.getId())) {
                UpdateQuizResultRequest.UpdateQuizResultQuizDto quizDto = quizDtoMap.get(quiz.getId());

                if (!quizDto.isAnswer()) {
                    quiz.addIncorrectAnswerCount();
                    quiz.updateIsReviewNeededTrueByIncorrectAnswer();
                }
                quizSetQuiz.updateIsAnswer(quizDto.isAnswer());
                quizSetQuiz.updateChoseAnswer(quizDto.getChoseAnswer());
                quizSetQuiz.updateElapsedTime(quizDto.getElapsedTime());
            }
        }
        quizSet.updateSolved();

        return quizSet.getQuizSetType();
    }

    public GetQuizAnswerRateAnalysisResponse findQuizAnswerRateAnalysis(
            Long memberId, Long directoryId, LocalDate startWeekDate, LocalDate startMonthDate
    ) {
        List<QuizSetQuiz> quizSetQuizzes;

        if (directoryId == null) {
            quizSetQuizzes = quizSetQuizRepository.findAllByMemberIdAndSolvedTrue(memberId);
        } else {
            quizSetQuizzes = quizSetQuizRepository.findAllByMemberIdAndDirectoryIdAndSolvedTrue(memberId, directoryId);
        }
        if (startMonthDate != null) {
            return quizAnalysisByMonth(quizSetQuizzes, startMonthDate);
        }

        if (startWeekDate == null) {
            startWeekDate = LocalDate.now().minusDays(6);
        }

        return quizAnalysisByWeek(quizSetQuizzes, startWeekDate);
    }

    @Transactional
    public void deleteQuiz(Long quizId, Long memberId) {
        Quiz quiz = quizRepository.findByQuizIdAndMemberId(quizId, memberId)
                .orElseThrow(() -> new CustomException(QUIZ_NOT_FOUND_ERROR));

        quizRepository.delete(quiz);
    }

    @Transactional
    public void deleteInvalidQuiz(Long quizId, Long memberId) {
        Quiz quiz = quizRepository.findByQuizIdAndMemberId(quizId, memberId)
                .orElseThrow(() -> new CustomException(QUIZ_NOT_FOUND_ERROR));

        quizRepository.delete(quiz);
    }

    @Transactional
    public CreateQuizzesResponse createMemberGeneratedQuizSet(Long documentId, Member member, String stringQuizType, Integer quizCount) {
        List<Quiz> quizzes = quizRepository.findByDocumentIdAndMemberId(documentId, member.getId());
        String quizSetName = quizzes.getFirst().getDocument().getName();

        if (stringQuizType.equals("RANDOM")) {
            Collections.shuffle(quizzes);
        } else {
            QuizType quizType = QuizType.valueOf(stringQuizType);
            quizzes = quizzes.stream()
                    .filter(quiz -> quiz.getQuizType() == quizType)
                    .toList();
        }

        if (quizCount > quizzes.size()) {
            throw new CustomException(QUIZ_COUNT_EXCEEDED);
        }

        quizzes = quizzes.subList(0, quizCount);

        List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();
        String quizSetId = createQuizSetId();
        QuizSet quizSet = QuizSet.createQuizSet(quizSetId, quizSetName, QuizSetType.DOCUMENT_QUIZ_SET, member);

        for (Quiz quiz : quizzes) {
            QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
            quizSetQuizzes.add(quizSetQuiz);
        }

        quizSetRepository.save(quizSet);
        quizSetQuizRepository.saveAll(quizSetQuizzes);

        return new CreateQuizzesResponse(quizSetId, QuizSetType.DOCUMENT_QUIZ_SET, LocalDateTime.now());
    }

    @Transactional
    public CreateQuizzesResponse createErrorCheckQuizSet(Long documentId, Member member) {
        List<Quiz> quizzes = quizRepository.findByDocumentIdAndMemberId(documentId, member.getId());
        String quizSetName = quizzes.getFirst().getDocument().getName();

        List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();
        String quizSetId = createQuizSetId();
        QuizSet quizSet = QuizSet.createQuizSet(quizSetId, quizSetName, QuizSetType.FIRST_QUIZ_SET, member);

        for (Quiz quiz : quizzes) {
            QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
            quizSetQuizzes.add(quizSetQuiz);
        }

        quizSetRepository.save(quizSet);
        quizSetQuizRepository.saveAll(quizSetQuizzes);

        return new CreateQuizzesResponse(quizSetId, QuizSetType.FIRST_QUIZ_SET, LocalDateTime.now());
    }

    @Transactional
    public CreateQuizzesResponse createCollectionQuizSet(Member member, Collection collection) {
        Set<CollectionQuiz> collectionQuizzes = collection.getCollectionQuizzes();
        String quizSetName = collection.getName();

        List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();
        String quizSetId = createQuizSetId();
        QuizSet quizSet = QuizSet.createQuizSet(quizSetId, quizSetName, QuizSetType.COLLECTION_QUIZ_SET, member);

        for (CollectionQuiz collectionQuiz : collectionQuizzes) {
            Quiz quiz = collectionQuiz.getQuiz();
            QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
            quizSetQuizzes.add(quizSetQuiz);
        }

        quizSetRepository.save(quizSet);
        quizSetQuizRepository.saveAll(quizSetQuizzes);

        return new CreateQuizzesResponse(quizSetId, QuizSetType.COLLECTION_QUIZ_SET, LocalDateTime.now());
    }

    public GetSingleQuizRecordByDateResponse findAllQuizSetRecordByDate(Long memberId, LocalDate solvedDate) {
        List<QuizSet> todayQuizSets = quizSetRepository.findAllByMemberIdAndSolvedTrueAndQuizSetTypeOrderByCreatedAtDesc(memberId, QuizSetType.TODAY_QUIZ_SET);
        int currentConsecutiveDays = checkCurrentConsecutiveTodayQuiz(todayQuizSets);
        int maxConsecutiveDays = checkMaxConsecutiveTodayQuiz(todayQuizSets);

        LocalDateTime startDateTime = solvedDate.atStartOfDay();
        LocalDateTime endDateTime = solvedDate.atTime(LocalTime.MAX);

        List<QuizSet> quizSets = quizSetRepository.findAllByMemberIdAndSolvedTrueAndDateTime(memberId, startDateTime, endDateTime);

        List<GetSingleQuizRecordByDateResponse.GetSingleQuizRecordsDto> quizRecordsDtos = new ArrayList<>();
        for (QuizSet quizSet : quizSets) {
            int quizCount = quizSet.getQuizSetQuizzes().size();
            int score = quizCount;
            List<QuizSetQuiz> quizSetQuizzes = quizSet.getQuizSetQuizzes();
            for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
                if (!quizSetQuiz.getIsAnswer()) {
                    score -= 1;
                }
            }

            GetSingleQuizRecordByDateResponse.GetSingleQuizRecordsDto quizRecordsDto = GetSingleQuizRecordByDateResponse.GetSingleQuizRecordsDto.builder()
                    .quizSetId(quizSet.getId())
                    .quizCount(quizCount)
                    .name(quizSet.getName())
                    .score(score)
                    .quizSetType(quizSet.getQuizSetType())
                    .build();

            quizRecordsDtos.add(quizRecordsDto);
        }
        return new GetSingleQuizRecordByDateResponse(currentConsecutiveDays, maxConsecutiveDays, quizRecordsDtos);
    }

    public GetQuizRecordsResponse findAllQuizAndCollectionRecords(Long memberId) {
        List<QuizSet> solvedQuizSets = quizSetRepository.findAllByMemberIdAndSolvedTrue(memberId);
        List<QuizSet> todayQuizSets = quizSetRepository.findAllByMemberIdAndSolvedTrueAndQuizSetTypeOrderByCreatedAtDesc(memberId, QuizSetType.TODAY_QUIZ_SET);
        int currentConsecutiveDays = checkCurrentConsecutiveTodayQuiz(todayQuizSets);
        int maxConsecutiveDays = checkMaxConsecutiveTodayQuiz(todayQuizSets);

        HashMap<LocalDate, List<QuizSet>> dateQuizSetsMap = new HashMap<>();

        for (QuizSet quizSet : solvedQuizSets) {
            LocalDate createdDate = quizSet.getCreatedAt().toLocalDate();

            dateQuizSetsMap.putIfAbsent(createdDate, new ArrayList<>());
            dateQuizSetsMap.get(createdDate).add(quizSet);
        }

        List<LocalDate> sortedDates = new ArrayList<>(dateQuizSetsMap.keySet());
        sortedDates.sort(Comparator.reverseOrder());

        List<GetQuizRecordsResponse.GetQuizRecordsSolvedDateDto> quizRecordSolvedDateDtos = new ArrayList<>();
        for (LocalDate createdDate : sortedDates) {
            List<QuizSet> quizSets = dateQuizSetsMap.get(createdDate);
            List<GetQuizRecordsResponse.GetQuizRecordsDto> quizRecordDtos = new ArrayList<>();
            for (QuizSet quizSet : quizSets) {
                int quizCount = quizSet.getQuizSetQuizzes().size();
                int score = quizCount;
                List<QuizSetQuiz> quizSetQuizzes = quizSet.getQuizSetQuizzes();
                for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
                    if (!quizSetQuiz.getIsAnswer()) {
                        score -= 1;
                    }
                }

                GetQuizRecordsResponse.GetQuizRecordsDto quizRecordDto = GetQuizRecordsResponse.GetQuizRecordsDto.builder()
                        .quizSetId(quizSet.getId())
                        .quizCount(quizCount)
                        .name(quizSet.getName())
                        .score(score)
                        .quizSetType(quizSet.getQuizSetType())
                        .build();

                quizRecordDtos.add(quizRecordDto);
            }
            GetQuizRecordsResponse.GetQuizRecordsSolvedDateDto quizRecordDto = GetQuizRecordsResponse.GetQuizRecordsSolvedDateDto.builder()
                    .solvedDate(createdDate)
                    .quizRecords(quizRecordDtos)
                    .build();

            quizRecordSolvedDateDtos.add(quizRecordDto);
        }

        return new GetQuizRecordsResponse(currentConsecutiveDays, maxConsecutiveDays, quizRecordSolvedDateDtos);
    }

    public GetSingleQuizSetRecordResponse findQuizSetRecordByMemberIdAndQuizSetId(Long memberId, String quizSetId, QuizSetType quizSetType) {
        List<QuizSetQuiz> quizSetQuizzes = quizSetQuizRepository.findAllByQuizSetIdAndMemberId(quizSetId, memberId);
        List<GetSingleQuizSetRecordResponse.GetSingleQuizSetRecordDto> quizSetRecordDtos = new ArrayList<>();

        int elapsedTimeMs = 0;
        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
            GetSingleQuizSetRecordResponse.GetSingleQuizSetRecordDto quizSetRecordDto;
            elapsedTimeMs += quizSetQuiz.getElapsedTimeMs();
            Quiz quiz = quizSetQuiz.getQuiz();

            List<String> optionList = new ArrayList<>();
            if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                Set<Option> options = quiz.getOptions();
                if (options.isEmpty()) {
                    continue;
                }
                for (Option option : options) {
                    optionList.add(option.getOption());
                }
            }

            if (quizSetType == QuizSetType.TODAY_QUIZ_SET || quizSetType == QuizSetType.DOCUMENT_QUIZ_SET || quizSetType == QuizSetType.FIRST_QUIZ_SET) {
                quizSetRecordDto = GetSingleQuizSetRecordResponse.GetSingleQuizSetRecordDto.builder()
                        .id(quiz.getId())
                        .question(quiz.getQuestion())
                        .answer(quiz.getAnswer())
                        .explanation(quiz.getExplanation())
                        .quizType(quiz.getQuizType())
                        .options(optionList)
                        .choseAnswer(quizSetQuiz.getChoseAnswer())
                        .isAnswer(quizSetQuiz.getIsAnswer())
                        .documentName(quiz.getDocument().getName())
                        .directoryName(quiz.getDocument().getDirectory().getName())
                        .quizSetType(quizSetQuiz.getQuizSet().getQuizSetType())
                        .build();
            } else {
                quizSetRecordDto = GetSingleQuizSetRecordResponse.GetSingleQuizSetRecordDto.builder()
                        .id(quiz.getId())
                        .question(quiz.getQuestion())
                        .answer(quiz.getAnswer())
                        .explanation(quiz.getExplanation())
                        .quizType(quiz.getQuizType())
                        .options(optionList)
                        .choseAnswer(quizSetQuiz.getChoseAnswer())
                        .isAnswer(quizSetQuiz.getIsAnswer())
                        .collectionName(quizSetQuiz.getQuizSet().getName())
                        .quizSetType(quizSetType)
                        .build();
            }

            quizSetRecordDtos.add(quizSetRecordDto);
        }
        LocalDateTime createdAt = quizSetQuizzes.getFirst().getQuizSet().getCreatedAt();

        return new GetSingleQuizSetRecordResponse(elapsedTimeMs, quizSetRecordDtos, createdAt);
    }

    public GetCurrentTodayQuizInfo findCurrentTodayQuizInfo(Long memberId) {
        List<QuizSet> quizSets = quizSetRepository.findAllByMemberIdAndSolvedTrueAndQuizSetTypeOrderByCreatedAtDesc(memberId, QuizSetType.TODAY_QUIZ_SET);
        int currentConsecutiveTodayQuizDate = checkCurrentConsecutiveTodayQuiz(quizSets);
        int maxConsecutiveTodayQuizDate = checkMaxConsecutiveTodayQuiz(quizSets);

        return new GetCurrentTodayQuizInfo(currentConsecutiveTodayQuizDate, maxConsecutiveTodayQuizDate);
    }

    public List<Quiz> findAllByDocumentIdAndMemberId(Long documentId, Long memberId) {
        return quizRepository.findAllByDocumentIdAndMemberId(documentId, memberId);
    }

    public int checkCurrentConsecutiveTodayQuiz(List<QuizSet> quizSets) {
        if (quizSets.isEmpty()) {
            return 0;
        }

        LocalDate firstQuizSetDate = quizSets.getFirst().getCreatedAt().toLocalDate();
        // 가장 최근 quizSet이 오늘이나 어제가 아니라면 return 0
        if (!firstQuizSetDate.equals(LocalDate.now()) && !firstQuizSetDate.equals(LocalDate.now().minusDays(1))) {
            return 0;
        }

        int currentConsecutiveDays = 0;
        LocalDate previousDate = null;

        for (QuizSet quizSet : quizSets) {
            LocalDate quizDate = quizSet.getCreatedAt().toLocalDate();

            if (previousDate == null) {
                currentConsecutiveDays++;
            } else if (previousDate.minusDays(1).equals(quizDate)) {
                currentConsecutiveDays++;
            } else {
                break;
            }
            previousDate = quizDate;
        }
        return currentConsecutiveDays;
    }

    public int checkMaxConsecutiveTodayQuiz(List<QuizSet> quizSets) {
        if (quizSets.isEmpty()) {
            return 0;
        }

        int currentConsecutiveDays = 0;
        int maxConsecutiveDays = 0;
        LocalDate previousDate = null;

        for (QuizSet quizSet : quizSets) {
            LocalDate quizDate = quizSet.getCreatedAt().toLocalDate();

            if (previousDate == null || previousDate.minusDays(1).equals(quizDate)) {
                // 연속된 날짜일 경우 currentConsecutiveDays 증가
                currentConsecutiveDays++;
            } else {
                // 연속되지 않으면 최대값을 갱신하고 현재 연속일 초기화
                maxConsecutiveDays = Math.max(maxConsecutiveDays, currentConsecutiveDays);
                currentConsecutiveDays = 1;  // 새로 시작된 연속일
            }

            previousDate = quizDate;  // 다음 비교를 위해 이전 날짜 업데이트
        }
        return Math.max(maxConsecutiveDays, currentConsecutiveDays);
    }

    @Transactional
    public void quizChunkBatchInsert(
            List<Quiz> quizzes, List<QuizSet> quizSets, List<QuizSetQuiz> quizSetQuizzes, List<Member> members) {
        String insertQuizSetSql = "INSERT INTO quiz_set (id, name, solved, quiz_set_type, member_id, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.batchUpdate(
                insertQuizSetSql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        QuizSet quizSet = quizSets.get(i);
                        ps.setString(1, quizSet.getId());
                        ps.setString(2, quizSet.getName());
                        ps.setBoolean(3, quizSet.isSolved());
                        ps.setObject(4, quizSet.getQuizSetType());
                        ps.setLong(5, quizSet.getMember().getId());
                        ps.setObject(6, LocalDateTime.now());
                        ps.setObject(7, LocalDateTime.now());
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

    public Quiz findQuizByQuizIdAndMemberId(Long quizId, Long memberId) {
        return quizRepository.findByQuizIdAndMemberId(quizId, memberId)
                .orElseThrow(() -> new CustomException(QUIZ_NOT_FOUND_ERROR));
    }

    public List<Quiz> findAllByMemberIdAndQuizIds(List<Long> quizIds, Long memberId) {
        return quizRepository.findAllByMemberIdAndQuizIds(memberId, quizIds);
    }

    // 7일 이내에 발생한 quizSet by memberId
    public List<QuizSetQuiz> findQuizSetQuizzesByMemberIdAndCreatedAtAfterSevenDaysAgo(Long memberId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return quizSetQuizRepository.findAllByMemberIdAndCreatedAtAfter(memberId, sevenDaysAgo);
    }

    public List<QuizSet> findAllByMemberIdAndSolvedTrueAndQuizSetTypeOrderByCreatedAtDesc(Long memberId) {
        return quizSetRepository.findAllByMemberIdAndSolvedTrueAndQuizSetTypeOrderByCreatedAtDesc(memberId, QuizSetType.TODAY_QUIZ_SET);
    }

    private static String createQuizSetId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    private HashMap<String, Integer> quizAnalysis(List<QuizSetQuiz> quizSetQuizzes) {
        HashMap<String, Integer> quizAnalysis = new HashMap<>();
        quizAnalysis.put("totalQuizCount", quizSetQuizzes.size());
        quizAnalysis.put("incorrectAnswerCount", 0);
        quizAnalysis.put("totalElapsedTimeMs", 0);

        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
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

    private GetQuizAnswerRateAnalysisResponse quizAnalysisByWeek(
            List<QuizSetQuiz> quizSetQuizzes, LocalDate startWeekDate) {
        HashMap<LocalDate, Integer> incorrectAnswerCountByDate = new LinkedHashMap<>();
        HashMap<LocalDate, Integer> totalQuizCountByDate = new LinkedHashMap<>();

        HashMap<String, Integer> quizAnalysis = quizAnalysis(quizSetQuizzes);
        Integer totalElapsedTimeMs = quizAnalysis.get("totalElapsedTimeMs");

        for (int i = 0; i <= 6; i++) {
            LocalDate date = startWeekDate.plusDays(i);
            incorrectAnswerCountByDate.put(date, 0);
            totalQuizCountByDate.put(date, 0);
        }

        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {
            LocalDate date = quizSetQuiz.getUpdatedAt().toLocalDate();

            if (!date.isBefore(startWeekDate) && !date.isAfter(startWeekDate.plusDays(7))) {
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
                        .quizCount(totalQuizCountByDate.getOrDefault(date, 0))
                        .incorrectAnswerCount(incorrectAnswerCountByDate.getOrDefault(date, 0))
                        .build();

                quizzesDtos.add(quizzesDto);
            }
        }
        return new GetQuizAnswerRateAnalysisResponse(
                totalElapsedTimeMs,
                quizzesDtos
        );
    }

    private GetQuizAnswerRateAnalysisResponse quizAnalysisByMonth(List<QuizSetQuiz> quizSetQuizzes, LocalDate startDateMonth) {
        HashMap<LocalDate, Integer> incorrectAnswerCountByDate = new LinkedHashMap<>();
        HashMap<LocalDate, Integer> totalQuizCountByDate = new LinkedHashMap<>();

        HashMap<String, Integer> quizAnalysis = quizAnalysis(quizSetQuizzes);
        Integer totalQuizCount = quizAnalysis.get("totalQuizCount");
        Integer totalIncorrectAnswerCount = quizAnalysis.get("totalIncorrectAnswerCount");
        Integer totalElapsedTimeMs = quizAnalysis.get("totalElapsedTimeMs");

        YearMonth yearMonth = YearMonth.of(startDateMonth.getYear(), startDateMonth.getMonth());
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
                    .quizCount(totalQuizCountByDate.get(date))
                    .incorrectAnswerCount(incorrectAnswerCountByDate.get(date))
                    .build();

            quizzesDtos.add(quizzesDto);
        }

        return new GetQuizAnswerRateAnalysisResponse(
                totalElapsedTimeMs,
                quizzesDtos
        );
    }

    @Transactional
    public void updateRandomQuizResult(List<UpdateRandomQuizResultRequest.UpdateRandomQuizResultDto> quizDtos, Long memberId) {
        List<Long> quizIds = new ArrayList<>();
        for (UpdateRandomQuizResultRequest.UpdateRandomQuizResultDto quizDto : quizDtos) {
            quizIds.add(quizDto.getId());
        }

        List<Quiz> quizzes = quizRepository.findAllByMemberIdAndQuizIds(memberId, quizIds);
        for (int i = 0; i <= quizDtos.size(); i++) {
            if (!quizDtos.get(i).isAnswer()) {
                quizzes.get(i).updateIsReviewNeededTrueByIncorrectAnswer();
            }
        }
    }

    @Transactional
    public void updateWrongQuizResult(List<UpdateRandomQuizResultRequest.UpdateRandomQuizResultDto> quizDtos, Long memberId) {
        List<Long> quizIds = new ArrayList<>();
        for (UpdateRandomQuizResultRequest.UpdateRandomQuizResultDto quizDto : quizDtos) {
            quizIds.add(quizDto.getId());
        }

        List<Quiz> quizzes = quizRepository.findAllByMemberIdAndQuizIds(memberId, quizIds);
        for (int i = 0; i <= quizDtos.size(); i++) {
            if (quizDtos.get(i).isAnswer()) {
                quizzes.get(i).updateIsReviewNeededFalseByCorrectAnswer();
            }
        }
    }

    public List<Quiz> findIncorrectQuizzesByMemberIdAndIsReviewNeedTrue(Long memberId) {
        return quizRepository.findAllByMemberIdAndIsReviewNeededTrue(memberId);
    }

    public List<QuizSet> findAllByMemberIdAndSolvedTrueAndQuizSetType(Long memberId, QuizSetType quizSetType) {
        return quizSetRepository.findAllByMemberIdAndSolvedTrueAndQuizSetType(memberId, quizSetType);
    }

    // 클라이언트 테스트 전용 API(실제 서비스 사용 X)
    @Transactional
    public CreateQuizzesResponse createTodayQuizForTest(Member member) {
        List<Quiz> quizzes = quizRepository.findAllByMemberIdForTest(member.getId());
        String quizSetName = quizzes.getFirst().getDocument().getName();
        Collections.shuffle(quizzes);

        List<QuizSetQuiz> quizSetQuizzes = new ArrayList<>();

        String quizSetId = createQuizSetId();
        QuizSet quizSet = QuizSet.createQuizSet(quizSetId, quizSetName, QuizSetType.TODAY_QUIZ_SET, member);

        for (Quiz quiz : quizzes) {
            quiz.addDeliveredCount();

            QuizSetQuiz quizSetQuiz = QuizSetQuiz.createQuizSetQuiz(quiz, quizSet);
            quizSetQuizzes.add(quizSetQuiz);
        }

        quizSetRepository.save(quizSet);
        quizSetQuizRepository.saveAll(quizSetQuizzes);

        return new CreateQuizzesResponse(quizSetId, QuizSetType.TODAY_QUIZ_SET, quizSet.getCreatedAt());
    }

    public void createSampleQuiz(Long documentId) {

    }
}
