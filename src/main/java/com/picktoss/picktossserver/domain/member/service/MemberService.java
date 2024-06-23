package com.picktoss.picktossserver.domain.member.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.member.controller.response.GetMemberInfoResponse;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.global.enums.SubscriptionPlanType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;
import static com.picktoss.picktossserver.domain.document.constant.DocumentConstant.*;
import static com.picktoss.picktossserver.domain.quiz.constant.QuizConstant.FREE_PLAN_QUIZ_QUESTION_NUM;
import static com.picktoss.picktossserver.domain.quiz.constant.QuizConstant.PRO_PLAN_QUIZ_QUESTION_NUM;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

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
                .name(member.getName())
                .email(email)
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
}
