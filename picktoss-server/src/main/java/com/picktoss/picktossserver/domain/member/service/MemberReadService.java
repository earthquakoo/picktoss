package com.picktoss.picktossserver.domain.member.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.core.s3.S3Provider;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.entity.DocumentBookmark;
import com.picktoss.picktossserver.domain.document.repository.DocumentBookmarkRepository;
import com.picktoss.picktossserver.domain.document.repository.DocumentRepository;
import com.picktoss.picktossserver.domain.member.dto.response.GetMemberInfoResponse;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.quiz.entity.DailyQuizRecordDetail;
import com.picktoss.picktossserver.domain.quiz.entity.QuizSetQuiz;
import com.picktoss.picktossserver.domain.quiz.repository.DailyQuizRecordDetailRepository;
import com.picktoss.picktossserver.domain.quiz.repository.QuizSetQuizRepository;
import com.picktoss.picktossserver.domain.star.entity.Star;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberReadService {

    private final S3Provider s3Provider;
    private final MemberRepository memberRepository;
    private final DocumentRepository documentRepository;
    private final DocumentBookmarkRepository documentBookmarkRepository;
    private final QuizSetQuizRepository quizSetQuizRepository;
    private final DailyQuizRecordDetailRepository dailyQuizRecordDetailRepository;

    public GetMemberInfoResponse findMemberInfo(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.MEMBER_NOT_FOUND));

        String imageUrl = null;
        if (member.getS3Key() != null && !member.getS3Key().isEmpty()) {
            imageUrl = s3Provider.findImage(member.getS3Key());
        }

        List<Document> documents = documentRepository.findAllByMemberId(memberId);
        List<DocumentBookmark> documentBookmarks = documentBookmarkRepository.findAllByMemberId(memberId);

        Star star = member.getStar();
        String email = Optional.ofNullable(member.getEmail()).orElse("");
        int monthlySolvedQuizCount = calculateMonthlySolvedQuizCount(memberId);

        Category category = member.getCategory();
        GetMemberInfoResponse.CategoryDto categoryDto;
        if (category != null) {
            categoryDto = GetMemberInfoResponse.CategoryDto.builder()
                    .id(category.getId())
                    .name(category.getName())
                    .emoji(category.getEmoji())
                    .build();
        } else {
            categoryDto = GetMemberInfoResponse.CategoryDto.builder()
                    .id(null)
                    .name(null)
                    .emoji(null)
                    .build();
        }

        return GetMemberInfoResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(email)
                .image(imageUrl)
                .category(categoryDto)
                .socialPlatform(member.getSocialPlatform())
                .star(star.getStar())
                .isQuizNotificationEnabled(member.isQuizNotificationEnabled())
                .totalQuizCount(documents.size())
                .bookmarkCount(documentBookmarks.size())
                .monthlySolvedQuizCount(monthlySolvedQuizCount)
                .build();
    }

    private int calculateMonthlySolvedQuizCount(Long memberId) {
        int monthlySolvedQuizCount = 0;

        LocalDate now = LocalDate.now();
        YearMonth yearMonth = YearMonth.of(now.getYear(), now.getMonth());
        LocalDateTime startOfDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfDate = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        List<QuizSetQuiz> quizSetQuizzes = quizSetQuizRepository.findAllByMemberIdAndSolvedTrueAndDateTime(memberId, startOfDate, endOfDate);
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
