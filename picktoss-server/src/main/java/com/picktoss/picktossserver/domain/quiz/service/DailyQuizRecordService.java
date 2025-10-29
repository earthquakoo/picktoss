package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.core.messagesource.MessageService;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.entity.DocumentBookmark;
import com.picktoss.picktossserver.domain.document.repository.DocumentBookmarkRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.quiz.dto.response.CreateDailyQuizRecordResponse;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetAllQuizzesResponse;
import com.picktoss.picktossserver.domain.quiz.entity.DailyQuizRecord;
import com.picktoss.picktossserver.domain.quiz.entity.DailyQuizRecordDetail;
import com.picktoss.picktossserver.domain.quiz.entity.Option;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.repository.DailyQuizRecordDetailRepository;
import com.picktoss.picktossserver.domain.quiz.repository.DailyQuizRecordRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import com.picktoss.picktossserver.domain.star.entity.Star;
import com.picktoss.picktossserver.domain.star.entity.StarHistory;
import com.picktoss.picktossserver.domain.star.repository.StarHistoryRepository;
import com.picktoss.picktossserver.global.enums.quiz.DailyQuizType;
import com.picktoss.picktossserver.global.enums.quiz.QuizSource;
import com.picktoss.picktossserver.global.enums.quiz.QuizType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DailyQuizRecordService {

    private final QuizRepository quizRepository;
    private final DailyQuizRecordRepository dailyQuizRecordRepository;
    private final DailyQuizRecordDetailRepository dailyQuizRecordDetailRepository;
    private final DocumentBookmarkRepository documentBookmarkRepository;
    private final MemberRepository memberRepository;
    private final StarHistoryRepository starHistoryRepository;

    private final MessageService messageService;

    public GetAllQuizzesResponse findQuizzes(Long memberId, DailyQuizType dailyQuizType, QuizSource quizSource, String language) {
        List<Quiz> quizzes = new ArrayList<>();
        List<DocumentBookmark> documentBookmarks = new ArrayList<>();

        boolean includeMyQuiz = quizSource == QuizSource.ALL || quizSource == QuizSource.MY_QUIZ;
        boolean includeBookmarkQuiz = quizSource == QuizSource.ALL || quizSource == QuizSource.BOOKMARK_QUIZ;

        QuizType quizType = null;
        if (dailyQuizType == DailyQuizType.MIX_UP) {
            quizType = QuizType.MIX_UP;
        } else if (dailyQuizType == DailyQuizType.MULTIPLE_CHOICE) {
            quizType = QuizType.MULTIPLE_CHOICE;
        }

        if (includeMyQuiz) {
            if (quizType == null) {
                quizzes = quizRepository.findAllByMemberIdAndLanguage(memberId, language);
            } else {
                quizzes = quizRepository.findAllByMemberIdAndQuizTypeAndLanguage(memberId, quizType, language);
            }
        }

        List<GetAllQuizzesResponse.GetAllQuizzesDto> quizzesDtos = new ArrayList<>();
        for (Quiz quiz : quizzes) {
            boolean isOwner = false;
            Member member = quiz.getDocument().getDirectory().getMember();
            if (Objects.equals(memberId, member.getId())) {
                isOwner = true;
            }

            List<String> optionList = new ArrayList<>();
            if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                Set<Option> options = quiz.getOptions();
                for (Option option : options) {
                    optionList.add(option.getOption());
                }
            }

            GetAllQuizzesResponse.GetAllQuizzesDto quizzesDto = GetAllQuizzesResponse.GetAllQuizzesDto.builder()
                    .id(quiz.getId())
                    .name(quiz.getDocument().getName())
                    .question(quiz.getQuestion())
                    .answer(quiz.getAnswer())
                    .explanation(quiz.getExplanation())
                    .isBookmarked(false)
                    .options(optionList)
                    .quizType(quiz.getQuizType())
                    .documentId(quiz.getDocument().getId())
                    .isOwner(isOwner)
                    .build();

            quizzesDtos.add(quizzesDto);
        }

        if (includeBookmarkQuiz) {
            if (quizType == null) {
                documentBookmarks = documentBookmarkRepository.findAllByMemberIdAndLanguage(memberId, language);
            } else {
                documentBookmarks = documentBookmarkRepository.findAllByMemberIdAndQuizTypeAndLanguage(memberId, quizType, language);
            }
        }

        for (DocumentBookmark documentBookmark : documentBookmarks) {
            Document document = documentBookmark.getDocument();
            Set<Quiz> quizSets = document.getQuizzes();
            for (Quiz quiz : quizSets) {
                boolean isOwner = false;
                Member member = quiz.getDocument().getDirectory().getMember();
                if (Objects.equals(memberId, member.getId())) {
                    isOwner = true;
                }

                List<String> optionList = new ArrayList<>();
                if (quiz.getQuizType() == QuizType.MULTIPLE_CHOICE) {
                    Set<Option> options = quiz.getOptions();
                    for (Option option : options) {
                        optionList.add(option.getOption());
                    }
                }

                GetAllQuizzesResponse.GetAllQuizzesDto quizzesDto = GetAllQuizzesResponse.GetAllQuizzesDto.builder()
                        .id(quiz.getId())
                        .name(quiz.getDocument().getName())
                        .question(quiz.getQuestion())
                        .answer(quiz.getAnswer())
                        .explanation(quiz.getExplanation())
                        .isBookmarked(true)
                        .options(optionList)
                        .quizType(quiz.getQuizType())
                        .documentId(quiz.getDocument().getId())
                        .isOwner(isOwner)
                        .build();

                quizzesDtos.add(quizzesDto);
            }
        }

        Collections.shuffle(quizzesDtos);

        return new GetAllQuizzesResponse(quizzesDtos);
    }

    @Transactional
    public CreateDailyQuizRecordResponse createDailyQuizRecord(Long memberId, Long quizId, String choseAnswer, Boolean isAnswer) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.MEMBER_NOT_FOUND));

        DailyQuizRecord dailyQuizRecord = checkPresentDailyQuizRecord(member);

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new CustomException(ErrorInfo.QUIZ_NOT_FOUND_ERROR));

        if (!isAnswer && Objects.equals(member.getId(), memberId)) {
            quiz.updateIsReviewNeededTrueByWrongAnswer();
        }

        DailyQuizRecordDetail dailyQuizRecordDetail = DailyQuizRecordDetail.createDailyQuizRecordDetail(isAnswer, choseAnswer, quiz, dailyQuizRecord);
        dailyQuizRecordDetailRepository.save(dailyQuizRecordDetail);

        int reward = 0;
        int consecutiveSolvedDailyQuizDays = calculateConsecutiveDailyQuiz(memberId);
        int todaySolvedDailyQuizCount = dailyQuizRecord.getDailyQuizRecordDetails().size();

        if (todaySolvedDailyQuizCount == 10) {
            consecutiveSolvedDailyQuizDays += 1;
            dailyQuizRecord.updateIsDailyQuizCompleteTrue();
            if (consecutiveSolvedDailyQuizDays % 5 == 0) {
                reward = 20;
            } else {
                reward = 5;

            }
            depositStarByDailyQuizComplete(member, reward);
        }

        return new CreateDailyQuizRecordResponse(reward, todaySolvedDailyQuizCount, consecutiveSolvedDailyQuizDays);
    }



    @Transactional
    private DailyQuizRecord checkPresentDailyQuizRecord(Member member) {
        Optional<DailyQuizRecord> optionalDailyQuizRecord = dailyQuizRecordRepository.findByMemberIdAndSolvedDate(member.getId(), LocalDate.now());
        if (optionalDailyQuizRecord.isEmpty()) {
            DailyQuizRecord dailyQuizRecord = DailyQuizRecord.createDailyQuizRecord(member);
            dailyQuizRecordRepository.save(dailyQuizRecord);
            return dailyQuizRecord;
        }
        return optionalDailyQuizRecord.get();
    }

    private int calculateConsecutiveDailyQuiz(Long memberId) {
        List<DailyQuizRecord> dailyQuizRecords = dailyQuizRecordRepository.findAllByMemberIdAndIsDailyQuizCompleteTrueOrderBySolvedDateDesc(memberId);
        if (dailyQuizRecords.isEmpty()) {
            return 0;
        }

        LocalDate firstQuizSetDate = dailyQuizRecords.getFirst().getSolvedDate().toLocalDate();
        if (!firstQuizSetDate.equals(LocalDate.now()) && !firstQuizSetDate.equals(LocalDate.now().minusDays(1))) {
            return 0;
        }

        LocalDate previousDate = null;
        int currentConsecutiveDays = 0;

        for (DailyQuizRecord dailyQuizRecord : dailyQuizRecords) {
            LocalDate solvedDate = dailyQuizRecord.getSolvedDate().toLocalDate();

            if (previousDate == null || previousDate.minusDays(1).equals(solvedDate)) {
                currentConsecutiveDays += 1;
            } else if (!previousDate.equals(solvedDate)) {
                break;
            }
            previousDate = solvedDate;
        }

        return currentConsecutiveDays;
    }

    @Transactional
    private void depositStarByDailyQuizComplete(Member member, int reward) {
        Star star = member.getStar();
        String description = messageService.getMessage("star.history.daily_quiz_reward");
        StarHistory starHistory = star.depositStarBySolvedDailyQuizReward(star, reward, description);
        starHistoryRepository.save(starHistory);
    }
}
