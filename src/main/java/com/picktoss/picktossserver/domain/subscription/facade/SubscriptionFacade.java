package com.picktoss.picktossserver.domain.subscription.facade;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.subscription.entity.Subscription;
import com.picktoss.picktossserver.domain.subscription.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionFacade {

    private final SubscriptionService subscriptionService;
    private final MemberService memberService;

    @Transactional
    public void createSubscription(Member member) {
        subscriptionService.createSubscription(member);
    }

    public Subscription findCurrentSubscription(String memberId) {
        Member member = memberService.findMemberById(memberId);
        return subscriptionService.findCurrentSubscription(memberId, member);
    }
}
