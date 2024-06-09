package com.picktoss.picktossserver.domain.member.facade;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtTokenDto;
import com.picktoss.picktossserver.domain.category.entity.Category;
import com.picktoss.picktossserver.domain.category.service.CategoryService;
import com.picktoss.picktossserver.domain.document.entity.Document;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.event.entity.Event;
import com.picktoss.picktossserver.domain.event.service.EventService;
import com.picktoss.picktossserver.domain.keypoint.service.KeyPointService;
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

import java.time.LocalDateTime;
import java.util.Optional;

import static com.picktoss.picktossserver.domain.document.constant.DocumentConstant.*;

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
    private final CategoryService categoryService;
    private final KeyPointService keyPointService;

    @Transactional
    public JwtTokenDto createMember(MemberInfoDto memberInfoDto) {
        Optional<Member> optionalMember = memberService.findMemberByGoogleClientId(memberInfoDto.getSub());

        if (optionalMember.isEmpty()) {
            Member member = memberInfoDto.toEntity();
            Long memberId = memberService.createMember(member);
            subscriptionService.createSubscription(member);
            eventService.createEvent(member);
            Category category = categoryService.createDefaultCategory(memberId, member);
            Document document = documentService.createDefaultDocument(category);
            keyPointService.createDefaultKeyPoint(document);
            return jwtTokenProvider.generateToken(memberId);
        }
        Long memberId = optionalMember.get().getId();
        return jwtTokenProvider.generateToken(memberId);
    }

    @Transactional
    public GetMemberInfoResponse findMemberInfo(Long memberId) {
        Member member = memberService.findMemberById(memberId);
        Subscription subscription = subscriptionService.findCurrentSubscription(memberId, member);

        Event event = eventService.findEventByMemberId(memberId);

        boolean isContinuousQuizDate = quizService.checkContinuousQuizDatesCount(memberId);
        if (!isContinuousQuizDate) {
            event.initContinuousSolvedQuizDateCount();
            event.updateUpdatedAt(LocalDateTime.now());
        }

        int continuousQuizDatesCount = event.getContinuousSolvedQuizDateCount();
        int maxContinuousQuizDatesCount = event.getMaxContinuousSolvedQuizDateCount();
        int point = event.getPoint();

        int possessDocumentCount = documentService.findPossessDocumentCount(memberId);
        int availableAiPickCount = AVAILABLE_AI_PICK_COUNT + subscription.getAvailableAiPickCount() - member.getAiPickCount();

        return memberService.findMemberInfo(
                member,
                subscription,
                possessDocumentCount,
                availableAiPickCount,
                point,
                continuousQuizDatesCount,
                maxContinuousQuizDatesCount
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
