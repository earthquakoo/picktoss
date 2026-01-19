package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetAllQuizRecordsResponse;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetConsecutiveSolvedDailyQuizDatesResponse;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetSingleDailyQuizRecordResponse;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetSingleQuizSetRecordResponse;
import com.picktoss.picktossserver.domain.quiz.entity.*;
import com.picktoss.picktossserver.domain.quiz.repository.DailyQuizRecordDetailRepository;
import com.picktoss.picktossserver.domain.quiz.repository.DailyQuizRecordRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetQuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetRepository;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import com.picktoss.picktossserver.global.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizRecordService {

    private final QuizSetRepository quizSetRepository;
    private final QuizSetQuizRepository quizSetQuizRepository;
    private final DailyQuizRecordRepository dailyQuizRecordRepository;
    private final DailyQuizRecordDetailRepository dailyQuizRecordDetailRepository;

    private final DateTimeUtil dateTimeUtil;

    public GetAllQuizRecordsResponse findAllQuizRecords(Long memberId, ZoneId memberZoneId) {
        List<QuizSet> quizSets = quizSetRepository.findAllByMemberIdAndSolvedTrueOrderByCreatedAtDesc(memberId);
        List<DailyQuizRecord> dailyQuizRecords = dailyQuizRecordRepository.findAllByMemberIdOrderBySolvedDateDesc(memberId);

        Map<LocalDate, List<GetAllQuizRecordsResponse.GetAllQuizRecordQuizSetDto>> quizSetGroupedByDate = new HashMap<>();
        for (QuizSet quizSet : quizSets) {
            if (quizSet.getCreatedAt() == null ||
                    quizSet.getQuizSetQuizzes() == null ||
                    quizSet.getQuizSetQuizzes().isEmpty()) {
                continue;
            }

            LocalDateTime quizSetLocalDateTime = dateTimeUtil.convertToMemberLocalDateTime(quizSet.getCreatedAt(), memberZoneId);

            Document document = quizSet.getQuizSetQuizzes().getFirst().getQuiz().getDocument();
            int correctCount = (int) quizSet.getQuizSetQuizzes().stream().filter(q -> Boolean.TRUE.equals(q.getIsAnswer())).count();

            GetAllQuizRecordsResponse.GetAllQuizRecordQuizSetDto dto = GetAllQuizRecordsResponse.GetAllQuizRecordQuizSetDto.builder()
                    .quizSetId(quizSet.getId())
                    .quizSetName(document.getName())
                    .emoji(document.getEmoji())
                    .totalQuizCount(quizSet.getQuizSetQuizzes().size())
                    .correctAnswerCount(correctCount)
                    .solvedDateTime(quizSetLocalDateTime)
                    .build();

            quizSetGroupedByDate.computeIfAbsent(quizSetLocalDateTime.toLocalDate(), k -> new ArrayList<>()).add(dto);
        }

        Map<LocalDate, List<GetAllQuizRecordsResponse.GetAllQuizRecordDailyQuizDto>> dailyQuizGroupedByDate = new HashMap<>();
        for (DailyQuizRecord dailyQuizRecord : dailyQuizRecords) {
            if (dailyQuizRecord.getDailyQuizRecordDetails() == null) {
                continue;
            }

            LocalDateTime solvedLocalDateTime = dateTimeUtil.convertToMemberLocalDateTime(dailyQuizRecord.getSolvedDate(), memberZoneId);

            GetAllQuizRecordsResponse.GetAllQuizRecordDailyQuizDto dto = GetAllQuizRecordsResponse.GetAllQuizRecordDailyQuizDto.builder()
                    .dailyQuizRecordId(dailyQuizRecord.getId())
                    .totalQuizCount(dailyQuizRecord.getDailyQuizRecordDetails().size())
                    .solvedDateTime(solvedLocalDateTime)
                    .build();

            dailyQuizGroupedByDate.computeIfAbsent(solvedLocalDateTime.toLocalDate(), k -> new ArrayList<>()).add(dto);
        }

        Set<LocalDate> allDates = new HashSet<>();
        allDates.addAll(quizSetGroupedByDate.keySet());
        allDates.addAll(dailyQuizGroupedByDate.keySet());

        List<GetAllQuizRecordsResponse.GetAllQuizRecordsDto> quizRecords = allDates.stream()
                .sorted(Comparator.reverseOrder())
                .map(date -> {
                    List<GetAllQuizRecordsResponse.GetAllQuizRecordQuizSetDto> sortedQuizSets =
                            quizSetGroupedByDate.getOrDefault(date, Collections.emptyList())
                                    .stream()
                                    .sorted(Comparator.comparing(GetAllQuizRecordsResponse.GetAllQuizRecordQuizSetDto::getSolvedDateTime))
                                    .collect(Collectors.toList());

                    List<GetAllQuizRecordsResponse.GetAllQuizRecordDailyQuizDto> sortedDailyQuizzes =
                            dailyQuizGroupedByDate.getOrDefault(date, Collections.emptyList())
                                    .stream()
                                    .sorted(Comparator.comparing(GetAllQuizRecordsResponse.GetAllQuizRecordDailyQuizDto::getSolvedDateTime))
                                    .collect(Collectors.toList());

                    return GetAllQuizRecordsResponse.GetAllQuizRecordsDto.builder()
                            .solvedDate(date)
                            .quizSets(sortedQuizSets)
                            .dailyQuizRecords(sortedDailyQuizzes)
                            .build();
                })
                .collect(Collectors.toList());

        return new GetAllQuizRecordsResponse(quizRecords);
    }

    public GetSingleQuizSetRecordResponse findSingleQuizSetRecord(Long memberId, Long quizSetId, ZoneId memberZoneId) {
        List<QuizSetQuiz> quizSetQuizzes = quizSetQuizRepository.findAllByQuizSetIdAndMemberIdAndSolvedTrue(quizSetId, memberId);
        if (quizSetQuizzes == null || quizSetQuizzes.isEmpty()) {
            throw new CustomException(ErrorInfo.UNRESOLVED_QUIZ_SET);
        }

        List<GetSingleQuizSetRecordResponse.GetSingleQuizSetRecordDto> quizSetRecordDtos = new ArrayList<>();

        int elapsedTimeMs = 0;
        int correctAnswerCount = 0;
        int totalQuizCount = quizSetQuizzes.size();
        for (QuizSetQuiz quizSetQuiz : quizSetQuizzes) {

            if (!Objects.isNull(quizSetQuiz.getIsAnswer()) && quizSetQuiz.getIsAnswer()) {
                correctAnswerCount += 1;
            }

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

            GetSingleQuizSetRecordResponse.GetSingleQuizSetRecordDto quizSetRecordDto = GetSingleQuizSetRecordResponse.GetSingleQuizSetRecordDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .quizType(quiz.getQuizType())
                    .options(optionList)
                    .choseAnswer(quizSetQuiz.getChoseAnswer())
                    .isAnswer(quizSetQuiz.getIsAnswer())
                    .build();

            quizSetRecordDtos.add(quizSetRecordDto);
        }

        double averageCorrectRate = (double) correctAnswerCount / (double) totalQuizCount * 100.0;
        QuizSet quizSet = quizSetQuizzes.getFirst().getQuizSet();
        LocalDateTime quizSetDateTime = dateTimeUtil.convertToMemberLocalDateTime(quizSet.getCreatedAt(), memberZoneId);

        Document document = quizSetQuizzes.getFirst().getQuiz().getDocument();

        return new GetSingleQuizSetRecordResponse(document.getName(), document.getEmoji(), totalQuizCount, elapsedTimeMs, averageCorrectRate, quizSetDateTime, quizSetRecordDtos);
    }

    public GetSingleDailyQuizRecordResponse findSingleDailyQuizRecord(Long dailyQuizRecordId, Long memberId) {
        List<DailyQuizRecordDetail> dailyQuizRecordDetails = dailyQuizRecordDetailRepository.findAllByDailyQuizRecordIdAndMemberId(dailyQuizRecordId, memberId);

        List<GetSingleDailyQuizRecordResponse.GetSingleDailyQuizRecordDto> quizDtos = new ArrayList<>();
        for (DailyQuizRecordDetail dailyQuizRecordDetail : dailyQuizRecordDetails) {
            Quiz quiz = dailyQuizRecordDetail.getQuiz();

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

            GetSingleDailyQuizRecordResponse.GetSingleDailyQuizRecordDto quizDto = GetSingleDailyQuizRecordResponse.GetSingleDailyQuizRecordDto.builder()
                    .id(quiz.getId())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .quizType(quiz.getQuizType())
                    .options(optionList)
                    .choseAnswer(dailyQuizRecordDetail.getChoseAnswer())
                    .isAnswer(dailyQuizRecordDetail.getIsAnswer())
                    .build();

            quizDtos.add(quizDto);
        }
        return new GetSingleDailyQuizRecordResponse(quizDtos);
    }

    public GetConsecutiveSolvedDailyQuizDatesResponse findConsecutiveSolvedQuizSetDates(Long memberId, LocalDate solvedDate, ZoneId memberZoneId) {
        List<DailyQuizRecord> dailyQuizRecords = dailyQuizRecordRepository.findAllByMemberIdAndIsDailyQuizCompleteTrueOrderBySolvedDate(memberId);

        HashMap<LocalDate, Boolean> solvedDailyQuizByDate = new LinkedHashMap<>();
        List<GetConsecutiveSolvedDailyQuizDatesResponse.GetDailyQuizRecordByDateDto> solvedQuizDateRecords = new ArrayList<>();

        YearMonth yearMonth = YearMonth.of(solvedDate.getYear(), solvedDate.getMonth());
        LocalDate startOfDate = yearMonth.atDay(1);
        LocalDate endOfDate = yearMonth.atEndOfMonth();

        for (int i = 0; i <= endOfDate.getDayOfMonth() - startOfDate.getDayOfMonth(); i++) {
            LocalDate date = startOfDate.plusDays(i);
            solvedDailyQuizByDate.put(date, false);
        }

        for (DailyQuizRecord dailyQuizRecord : dailyQuizRecords) {
            LocalDate dailyQuizRecordDate = dateTimeUtil.convertToMemberLocalDate(dailyQuizRecord.getSolvedDate(), memberZoneId);
            solvedDailyQuizByDate.put(dailyQuizRecordDate, true);
        }

        for (LocalDate localDate : solvedDailyQuizByDate.keySet()) {
            Boolean isDailyQuizComplete = solvedDailyQuizByDate.get(localDate);
            GetConsecutiveSolvedDailyQuizDatesResponse.GetDailyQuizRecordByDateDto quizRecordByDateDto = GetConsecutiveSolvedDailyQuizDatesResponse.GetDailyQuizRecordByDateDto.builder()
                    .date(localDate)
                    .isDailyQuizComplete(isDailyQuizComplete)
                    .build();

            solvedQuizDateRecords.add(quizRecordByDateDto);
        }

        return new GetConsecutiveSolvedDailyQuizDatesResponse(solvedQuizDateRecords);
    }

    public int calculateConsecutiveDailyQuiz(Long memberId, ZoneId memberZoneId) {
        List<DailyQuizRecord> dailyQuizRecords = dailyQuizRecordRepository.findAllByMemberIdAndIsDailyQuizCompleteTrueOrderBySolvedDateDesc(memberId);
        if (dailyQuizRecords.isEmpty()) {
            return 0;
        }

        LocalDate memberToday = LocalDate.now(memberZoneId);
        LocalDate memberYesterday = memberToday.minusDays(1);

        LocalDateTime firstRecordUtc = dailyQuizRecords.getFirst().getSolvedDate();
        LocalDate firstQuizSetDate = dateTimeUtil.convertToMemberLocalDate(firstRecordUtc, memberZoneId);


        if (!firstQuizSetDate.equals(memberToday) && !firstQuizSetDate.equals(memberYesterday)) {
            return 0;
        }

        LocalDate previousDate = null;
        int currentConsecutiveDays = 0;

        for (DailyQuizRecord dailyQuizRecord : dailyQuizRecords) {
            LocalDateTime solvedUtc = dailyQuizRecord.getSolvedDate();
            LocalDate solvedDate = dateTimeUtil.convertToMemberLocalDate(solvedUtc, memberZoneId);

            if (previousDate == null || previousDate.minusDays(1).equals(solvedDate)) {
                currentConsecutiveDays += 1;
            } else if (!previousDate.equals(solvedDate)) {
                break;
            }
            previousDate = solvedDate;
        }

        return currentConsecutiveDays;
    }
}
