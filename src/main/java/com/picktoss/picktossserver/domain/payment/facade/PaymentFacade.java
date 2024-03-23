package com.picktoss.picktossserver.domain.payment.facade;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentFacade {

    private final PaymentService paymentService;
    private final MemberService memberService;

    public void createPayment(String name, String memberId) {
        Member member = memberService.findMemberById(memberId);
        paymentService.createPayment(name, member);
    }
}
