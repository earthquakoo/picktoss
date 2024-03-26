package com.picktoss.picktossserver.domain.member.service;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtTokenDto;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.member.controller.response.GetMemberInfoResponse;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.controller.dto.MemberInfoDto;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.domain.subscription.repository.SubscriptionRepository;
import com.picktoss.picktossserver.domain.subscription.service.SubscriptionService;
import com.picktoss.picktossserver.global.enums.SubscriptionPlanType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;
import static com.picktoss.picktossserver.domain.document.constant.DocumentConstant.*;
import static com.picktoss.picktossserver.domain.question.constant.QuestionConstant.*;

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
            Long memberId,
            Subscription subscription,
            int currentSubscriptionUploadedDocumentNum,
            int currentUploadedDocumentNum) {

        Member member = findMemberById(memberId);

        GetMemberInfoResponse.DocumentDto documentDto = GetMemberInfoResponse.DocumentDto.builder()
                .currentPossessDocumentNum(currentSubscriptionUploadedDocumentNum)
                .currentSubscriptionCycleUploadedDocumentNum(currentUploadedDocumentNum)
                .freePlanMaxPossessDocumentNum(FREE_PLAN_CURRENT_MAX_DOCUMENT_NUM)
                .freePlanSubscriptionMaxUploadDocumentNum(FREE_PLAN_MONTHLY_MAX_DOCUMENT_NUM)
                .proPlanMaxPossessDocumentNum(PRO_PLAN_CURRENT_MAX_DOCUMENT_NUM)
                .proPlanSubscriptionMaxUploadDocumentNum(PRO_PLAN_MONTHLY_MAX_DOCUMENT_NUM)
                .build();

        GetMemberInfoResponse.QuizDto quizDto = GetMemberInfoResponse.QuizDto.builder()
                .freePlanQuizQuestionNum(FREE_PLAN_QUIZ_QUESTION_NUM)
                .proPlanQuizQuestionNum(PRO_PLAN_QUIZ_QUESTION_NUM)
                .build();

        GetMemberInfoResponse.SubscriptionDto subscriptionDto = GetMemberInfoResponse.SubscriptionDto.builder()
                .plan(subscription.getSubscriptionPlanType())
                .purchasedDate(subscription.getPurchasedDate())
                .expireDate(subscription.getExpireDate())
                .build();

        return GetMemberInfoResponse.builder()
                .email(member.getEmail())
                .documentUsage(documentDto)
                .quiz(quizDto)
                .subscription(subscriptionDto)
                .build();
    }

    public Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));
    }

    public Optional<Member> findMemberByGoogleClientId(String googleClientId) {
        return memberRepository.findByGoogleClientId(googleClientId);
    }
}
