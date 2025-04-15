package com.picktoss.picktossserver.domain.member.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.document.entity.DocumentBookmark;
import com.picktoss.picktossserver.domain.document.repository.DocumentBookmarkRepository;
import com.picktoss.picktossserver.domain.member.dto.response.GetMemberInfoResponse;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.quiz.entity.DailyQuizRecordDetail;
import com.picktoss.picktossserver.domain.quiz.entity.Quiz;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.repository.DailyQuizRecordDetailRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetQuizRepository;
import com.picktoss.picktossserver.domain.star.entity.Star;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberReadService {

    private final MemberRepository memberRepository;
    private final DocumentBookmarkRepository documentBookmarkRepository;
    private final QuizSetQuizRepository quizSetQuizRepository;
    private final DailyQuizRecordDetailRepository dailyQuizRecordDetailRepository;
    private final QuizRepository quizRepository;

    public GetMemberInfoResponse findMemberInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.MEMBER_NOT_FOUND));

        List<Quiz> quizzes = quizRepository.findAllByMemberId(memberId);
        List<DocumentBookmark> documentBookmarks = documentBookmarkRepository.findAllByMemberId(memberId);

        Star star = member.getStar();
        String email = Optional.ofNullable(member.getEmail()).orElse("");
        int monthlySolvedQuizCount = calculateMonthlySolvedQuizCount(memberId);

        return GetMemberInfoResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(email)
                .category(member.getCategory())
                .socialPlatform(member.getSocialPlatform())
                .star(star.getStar())
                .isQuizNotificationEnabled(member.isQuizNotificationEnabled())
                .totalQuizCount(quizzes.size())
                .bookmarkCount(documentBookmarks.size())
                .monthlySolvedQuizCount(monthlySolvedQuizCount)
                .build();
    }

    private int calculateMonthlySolvedQuizCount(Long memberId) {
        int monthlySolvedQuizCount = 0;

        LocalDate now = LocalDate.now();
        YearMonth yearMonth = YearMonth.of(now.getYear(), now.getMonth());
        LocalDate startOfDate = yearMonth.atDay(1);
        LocalDate endOfDate = yearMonth.atEndOfMonth();

        List<QuizSetQuiz> quizSetQuizzes = quizSetQuizRepository.findAllByMemberIdAndSolvedTrueAndDateTime(memberId, startOfDate.atStartOfDay(), endOfDate.atTime(LocalTime.MAX));
        List<DailyQuizRecordDetail> dailyQuizRecordDetails = dailyQuizRecordDetailRepository.findAllByMemberIdAndDate(memberId, startOfDate, endOfDate);

        if (!quizSetQuizzes.isEmpty()) {
            monthlySolvedQuizCount += quizSetQuizzes.size();
        }

        if (!dailyQuizRecordDetails.isEmpty()) {
            monthlySolvedQuizCount += dailyQuizRecordDetails.size();
        }

        return monthlySolvedQuizCount;
    }
}
