package com.picktoss.picktossserver.domain.member.facade;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtTokenDto;
import com.picktoss.picktossserver.domain.auth.controller.dto.GoogleMemberDto;
import com.picktoss.picktossserver.domain.auth.controller.dto.KakaoMemberDto;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.event.entity.Event;
import com.picktoss.picktossserver.domain.event.service.EventService;
import com.picktoss.picktossserver.domain.member.controller.dto.MemberInfoDto;
import com.picktoss.picktossserver.domain.member.controller.response.GetMemberInfoResponse;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.quiz.service.QuizService;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.domain.subscription.service.SubscriptionService;
import com.picktoss.picktossserver.global.enums.SubscriptionPlanType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.picktoss.picktossserver.domain.document.constant.DocumentConstant.FREE_PLAN_DEFAULT_DOCUMENT_COUNT;
import static com.picktoss.picktossserver.domain.document.constant.DocumentConstant.FREE_PLAN_MONTHLY_DOCUMENT_COUNT;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberFacade {

    private final JwtTokenProvider jwtTokenProvider;
    private final MemberService memberService;
    private final DocumentService documentService;
    private final SubscriptionService subscriptionService;
    private final QuizService quizService;
    private final EventService eventService;

    @Transactional
    public JwtTokenDto createMember(MemberInfoDto memberInfoDto) {
        Optional<Member> optionalMember = memberService.findMemberByGoogleClientId(memberInfoDto.getSub());

        if (optionalMember.isEmpty()) {
            Member member = memberInfoDto.toEntity();
            memberService.createMember(member);
            subscriptionService.createSubscription(member);
            eventService.createEvent(member);
            return jwtTokenProvider.generateToken(member.getId());
        }
        Long memberId = optionalMember.get().getId();
        return jwtTokenProvider.generateToken(memberId);
    }

    @Transactional
    public GetMemberInfoResponse findMemberInfo(Long memberId) {
        Member member = memberService.findMemberById(memberId);
        Subscription subscription = subscriptionService.findCurrentSubscription(memberId, member);

        Event event = eventService.findEventByMemberId(memberId);
        eventService.checkContinuousQuizSolvedDate(memberId);

        int point = event.getPoint();
        int continuousQuizDatesCount = event.getContinuousSolvedQuizDateCount();

        int possessDocumentCount = documentService.findPossessDocumentCount(memberId);
        int uploadedDocumentCount = documentService.findUploadedDocumentCount(memberId);
        int possibleUploadedDocumentCount = FREE_PLAN_DEFAULT_DOCUMENT_COUNT + subscription.getUploadedDocumentCount() - uploadedDocumentCount;


        return memberService.findMemberInfo(
                member,
                subscription,
                possessDocumentCount,
                possibleUploadedDocumentCount,
                point,
                continuousQuizDatesCount
        );
    }

    @Transactional
    public void updateMemberName(Long memberId, String name) {
        memberService.updateMemberName(memberId, name);
    }

    @Transactional
    public void updateQuizNotification(Long memberId, boolean isQuizNotification) {
        memberService.updateQuizNotification(memberId, isQuizNotification);
    }

    private static int getPossibleUploadedDocumentCount(Subscription subscription, int uploadedDocumentCount, int uploadedDocumentCountForCurrentSubscription) {
        int possibleUploadedDocumentCount = FREE_PLAN_DEFAULT_DOCUMENT_COUNT - uploadedDocumentCount;

        if (subscription.getSubscriptionPlanType() == SubscriptionPlanType.FREE) {
            if (uploadedDocumentCount >= FREE_PLAN_DEFAULT_DOCUMENT_COUNT &&
                    uploadedDocumentCountForCurrentSubscription <= FREE_PLAN_MONTHLY_DOCUMENT_COUNT) {
                possibleUploadedDocumentCount = FREE_PLAN_MONTHLY_DOCUMENT_COUNT - uploadedDocumentCountForCurrentSubscription;
            }
        }
        return possibleUploadedDocumentCount;
    }
}
