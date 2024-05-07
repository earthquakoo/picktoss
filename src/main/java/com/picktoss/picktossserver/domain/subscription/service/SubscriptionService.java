package com.picktoss.picktossserver.domain.subscription.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.document.service.DocumentService;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.domain.subscription.repository.SubscriptionRepository;
import com.picktoss.picktossserver.global.enums.SubscriptionPlanType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.PRO_PLAN_ANYTIME_DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR;
import static com.picktoss.picktossserver.domain.document.constant.DocumentConstant.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;

    @Transactional
    public void createSubscription(Member member) {
        Subscription subscription = Subscription.builder()
                .subscriptionPlanType(SubscriptionPlanType.FREE)
                .member(member)
                .purchasedDate(LocalDateTime.now())
                .expireDate(LocalDateTime.now().plusDays(30))
                .build();
        subscriptionRepository.save(subscription);
    }

    @Transactional
    public Subscription findCurrentSubscription(Long memberId, Member member) {
        List<Subscription> subscriptions = subscriptionRepository.findAllByMemberId(memberId);

        Subscription latestSubscription = subscriptions.stream()
                .sorted(Comparator.comparing(Subscription::getPurchasedDate).reversed())
                .toList()
                .getFirst();

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(latestSubscription.getExpireDate())) {
            LocalDateTime markExpireDate = latestSubscription.getExpireDate();
            while (now.isAfter(markExpireDate.plusDays(30))) {
                markExpireDate = markExpireDate.plusDays(30);
            }

            Subscription subscription = Subscription.builder()
                    .subscriptionPlanType(SubscriptionPlanType.FREE)
                    .purchasedDate(markExpireDate)
                    .expireDate(markExpireDate.plusDays(30))
                    .member(member)
                    .build();

            subscriptionRepository.save(subscription);
        }
        return latestSubscription;
    }

    public void checkDocumentUploadLimit(
            Subscription currentSubscription,
            int possessDocumentCount, // 보유한 모든 문서 개수
            int uploadedDocumentCount, // 생성한 모든 문서 개수
            int currentSubscriptionUploadedDocumentCount // 현재 구독 사이클에 업로드한 문서 개수
    ) {
        if (currentSubscription.getSubscriptionPlanType() == SubscriptionPlanType.FREE) {
            if (possessDocumentCount >= FREE_PLAN_MAX_POSSESS_DOCUMENT_COUNT) {
                throw new CustomException(FREE_PLAN_ANYTIME_DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR);
            }
            if (uploadedDocumentCount >= FREE_PLAN_DEFAULT_DOCUMENT_COUNT) {
                if (currentSubscriptionUploadedDocumentCount >= FREE_PLAN_MONTHLY_DOCUMENT_COUNT) {
                    throw new CustomException(FREE_PLAN_ANYTIME_DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR);
                }
            }
        } else if (currentSubscription.getSubscriptionPlanType() == SubscriptionPlanType.PRO) {
            if (currentSubscriptionUploadedDocumentCount >= PRO_PLAN_MONTHLY_DOCUMENT_COUNT) { // 40개
                throw new CustomException(PRO_PLAN_CURRENT_SUBSCRIPTION_DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR);
            }
        } else {
            throw new IllegalArgumentException("Invalid Plan Type");
        }

//        if (currentSubscription.getSubscriptionPlanType() == SubscriptionPlanType.FREE) {
//            if (numCurrentSubscriptionUploadedDocuments >= FREE_PLAN_MONTHLY_MAX_DOCUMENT_NUM) { // 15개
//                throw new CustomException(FREE_PLAN_CURRENT_SUBSCRIPTION_DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR);
//            }
//            if (numCurrentUploadedDocuments >= FREE_PLAN_MONTHLY_MAX_DOCUMENT_NUM) { // 매 시점: 3개
//                throw new CustomException(FREE_PLAN_ANYTIME_DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR);
//            }
//        } else if (currentSubscription.getSubscriptionPlanType() == SubscriptionPlanType.PRO) {
//            if (numCurrentSubscriptionUploadedDocuments >= PRO_PLAN_MONTHLY_MAX_DOCUMENT_NUM) { // 40개
//                throw new CustomException(PRO_PLAN_CURRENT_SUBSCRIPTION_DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR);
//            }
//            if (numCurrentUploadedDocuments >= PRO_PLAN_CURRENT_MAX_DOCUMENT_NUM) { // 매 시점: 15개
//                throw new CustomException(PRO_PLAN_ANYTIME_DOCUMENT_UPLOAD_LIMIT_EXCEED_ERROR);
//            }
//        } else {
//            throw new IllegalArgumentException("Invalid Plan Type");
//        }
    }
}
