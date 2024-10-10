package com.picktoss.picktossserver.domain.member.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.member.controller.response.GetMemberInfoResponse;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.global.enums.MemberRole;
import com.picktoss.picktossserver.global.enums.SocialPlatform;
import jakarta.persistence.Tuple;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.MEMBER_NOT_FOUND;
import static com.picktoss.picktossserver.domain.document.constant.DocumentConstant.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public Long createMember(Member member) {
        memberRepository.save(member);
        return member.getId();
    }

    public GetMemberInfoResponse findMemberInfo(
            Member member,
            Subscription subscription,
            int possessDocumentCount,
            int availableAiPickCount,
            int point,
            int continuousQuizDatesCount,
            int maxContinuousQuizDatesCount
    ) {

        GetMemberInfoResponse.GetMemberInfoDocumentDto documentDto = GetMemberInfoResponse.GetMemberInfoDocumentDto.builder()
                .possessDocumentCount(possessDocumentCount)
                .availableAiPickCount(availableAiPickCount)
                .freePlanMaxPossessDocumentCount(FREE_PLAN_MAX_POSSESS_DOCUMENT_COUNT)
                .freePlanMonthlyAvailableAiPickCount(FREE_PLAN_MONTHLY_AVAILABLE_AI_PICK_COUNT)
                .proPlanMonthlyAvailableAiPickCount(PRO_PLAN_MONTHLY_AVAILABLE_AI_PICK_COUNT)
                .build();

        GetMemberInfoResponse.GetMemberInfoSubscriptionDto subscriptionDto = GetMemberInfoResponse.GetMemberInfoSubscriptionDto.builder()
                .plan(subscription.getSubscriptionPlanType())
                .purchasedDate(subscription.getPurchasedDate())
                .expireDate(subscription.getExpireDate())
                .build();

        String email = Optional.ofNullable(member.getEmail()).orElse("");

        return GetMemberInfoResponse.builder()
                .id(member.getId())
                .name(member.getName())
                .email(email)
                .role(member.getRole())
                .point(point)
                .continuousQuizDatesCount(continuousQuizDatesCount)
                .maxContinuousQuizDatesCount(maxContinuousQuizDatesCount)
                .documentUsage(documentDto)
                .subscription(subscriptionDto)
                .isQuizNotificationEnabled(member.isQuizNotificationEnabled())
                .build();
    }

    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
    }

    public Optional<Member> findMemberByGoogleClientId(String googleClientId) {
        return memberRepository.findByClientId(googleClientId);
    }

    public Member findMemberByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
    }

    public Optional<Member> findMemberByClientId(String clientId) {
        return memberRepository.findByClientId(clientId);
    }

    @Transactional
    public void deleteMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        memberRepository.delete(member);
    }

    @Transactional
    public void updateMemberName(Long memberId, String name) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        member.updateMemberName(name);
    }

    @Transactional
    public void updateQuizNotification(Long memberId, boolean isQuizNotification) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        member.updateQuizNotification(isQuizNotification);
    }

    // 클라이언트 테스트 전용 API(실제 서비스 사용 X)
    @Transactional
    public void changeAiPickCountForTest(Long memberId, int aiPickCount) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        member.changeAiPickCountForTest(aiPickCount);
    }

    @Transactional
    public void createMemberForTest() {
        String nickname = "test Member";
        String clientId = "test client id";

        List<Member> members = new ArrayList<>();

        for (int i = 0; i < 999; i++) {
            Member member = Member.builder()
                    .name(nickname)
                    .clientId(clientId)
                    .socialPlatform(SocialPlatform.GOOGLE)
                    .isQuizNotificationEnabled(false)
                    .aiPickCount(AVAILABLE_AI_PICK_COUNT)
                    .role(MemberRole.ROLE_USER)
                    .build();

            members.add(member);
        }

        String insertQuizSetQuizzesSql = "INSERT INTO member (name, client_id, social_platform, is_quiz_notification_enabled, ai_pick_count, role, created_at, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        jdbcTemplate.batchUpdate(
                insertQuizSetQuizzesSql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setObject(1, nickname);
                        ps.setObject(2, clientId);
                        ps.setObject(3, "GOOGLE");
                        ps.setObject(4, false);
                        ps.setObject(5, AVAILABLE_AI_PICK_COUNT);
                        ps.setObject(6, "ROLE_USER");
                        ps.setObject(7, LocalDateTime.now());
                        ps.setObject(8, LocalDateTime.now());
                    }

                    @Override
                    public int getBatchSize() {
                        return members.size();
                    }
                }
        );
    }

    public Member findMemberWithCollectionSolvedRecordByMemberId(Long memberId) {
        return memberRepository.findMemberWithCollectionSolvedRecordByMemberId(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
    }

    public Tuple findMinIdAndMaxIdAndIsQuizNotificationEnabled() {
        return memberRepository.findMinIdAndMaxIdAndIsQuizNotificationEnabled();
    }
}
