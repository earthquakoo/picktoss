package com.picktoss.picktossserver.domain.payment.service;


import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import com.picktoss.picktossserver.domain.payment.entity.Payment;
import com.picktoss.picktossserver.domain.payment.repository.PaymentRepository;
import com.picktoss.picktossserver.domain.star.entity.Star;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.domain.subscription.repository.SubscriptionRepository;
import com.siot.IamportRestClient.IamportClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentCreateService {

    private final IamportClient iamportClient;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final SubscriptionRepository subscriptionRepository;


    @Transactional
    public void createPayments(String impUid, int amount, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.MEMBER_NOT_FOUND));

        Payment payment = Payment.createPayment(impUid, amount, member);
        paymentRepository.save(payment);

        upgradeSubscriptionPlanType(memberId);

        updateIsUnlimitedStarsForSubscriptionProType(member);
    }

    @Transactional
    private void upgradeSubscriptionPlanType(Long memberId) {
        Subscription subscription = subscriptionRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorInfo.SUBSCRIPTION_NOT_FOUND));

        LocalDateTime localDateTime = LocalDateTime.now().plusDays(30);

        subscription.processSubscriptionPlanTypeUpgradeOnPayment(localDateTime);
    }

    @Transactional
    private void updateIsUnlimitedStarsForSubscriptionProType(Member member) {
        Star star = member.getStar();

        star.updateIsUnlimitedStarsForSubscriptionProType();
    }
}
