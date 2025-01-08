package com.picktoss.picktossserver.domain.quiz.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.collection.entity.CollectionQuizSet;
import com.picktoss.picktossserver.domain.collection.repository.CollectionQuizSetRepository;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetCurrentTodayQuizInfo;
import com.picktoss.picktossserver.domain.quiz.dto.response.GetQuizSetTodayResponse;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSet;
import com.picktoss.picktossserver.domain.quiz.entity.RandomQuizRecord;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetRepository;
import com.picktoss.picktossserver.domain.quiz.repository.RandomQuizRecordRepository;
import com.picktoss.picktossserver.domain.quiz.util.QuizUtil;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetResponseType;
import com.picktoss.picktossserver.global.enums.quiz.QuizSetType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodayQuizService {

    private final QuizUtil quizUtil;
    private final QuizSetRepository quizSetRepository;
    private final DocumentRepository documentRepository;
    private final MemberRepository memberRepository;
    private final RandomQuizRecordRepository randomQuizRecordRepository;
    private final CollectionQuizSetRepository collectionQuizSetRepository;

    public GetQuizSetTodayResponse findQuizSetToday(Long memberId) {
        List<Document> documents = documentRepository.findAllByMemberId(memberId);
        if (documents.isEmpty()) {
            return GetQuizSetTodayResponse.builder()
                    .type(QuizSetResponseType.NOT_READY)
                    .build();
        }
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

    public GetCurrentTodayQuizInfo findCurrentTodayQuizInfo(Long memberId) {
        List<QuizSet> solvedQuizSets = quizSetRepository.findAllByMemberIdAndSolvedTrue(memberId);
        int currentConsecutiveTodayQuizDate = quizUtil.checkCurrentConsecutiveSolvedQuizSet(solvedQuizSets);
        int maxConsecutiveTodayQuizDate = quizUtil.checkMaxConsecutiveSolvedQuizSet(solvedQuizSets);

        return new GetCurrentTodayQuizInfo(currentConsecutiveTodayQuizDate, maxConsecutiveTodayQuizDate);
    }

    public int findTodaySolvedQuizCount(Long memberId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStartTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MIN);
        LocalDateTime todayEndTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);

        int todaySolvedQuizCount = 0;

        List<QuizSet> quizSets = quizSetRepository.findAllByMemberIdAndSolvedTrueAndDateTime(memberId, todayStartTime, todayEndTime);
        for (QuizSet quizSet : quizSets) {
            todaySolvedQuizCount += quizSet.getQuizSetQuizzes().size();
        }

        List<CollectionQuizSet> collectionQuizSets = collectionQuizSetRepository.findAllByMemberIdAndSolvedTrueAndDateTime(memberId, todayStartTime, todayEndTime);
        for (CollectionQuizSet collectionQuizSet : collectionQuizSets) {
            todaySolvedQuizCount += collectionQuizSet.getCollectionQuizSetCollectionQuizzes().size();
        }

        List<RandomQuizRecord> randomQuizRecords = randomQuizRecordRepository.findAllByMemberIdAndCreatedAtBetween(memberId, todayStartTime, todayEndTime);
        for (RandomQuizRecord randomQuizRecord : randomQuizRecords) {
            todaySolvedQuizCount += randomQuizRecord.getSolvedQuizCount();
        }
        return todaySolvedQuizCount;
    }

    public void findTodaySolvedQuizCountByMember(Long memberId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStartTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MIN);
        LocalDateTime todayEndTime = LocalDateTime.of(now.toLocalDate(), LocalTime.MAX);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.MEMBER_NOT_FOUND));

        List<QuizSet> quizSets = member.getQuizSets();
        List<CollectionQuizSet> collectionQuizSets = member.getCollectionQuizSets();
        List<RandomQuizRecord> randomQuizRecords = member.getRandomQuizRecords();
    }
}
