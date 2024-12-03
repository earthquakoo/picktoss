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

    public void tempSaveAmount(String orderId, Integer amount) {
        paymentService.tempSaveAmount(orderId, amount);
    }

    public void verifyAmount(String orderId, Integer amount) {
        paymentService.verifyAmount(orderId, amount);
    }

    @Transactional
    public void confirmPayment(String paymentKey, String orderId, Integer amount, Long memberId) {
        Member member = memberService.findMemberById(memberId);
        paymentService.confirmPayment(paymentKey, orderId, amount, member);
    }

    public void cancelPayment(String paymentKey) {
        paymentService.cancelPayment(paymentKey);
    }

    public void findPaymentsByOrderId(String orderId) {
        paymentService.findPaymentsByOrderId(orderId);
    }

    public void findPaymentsByPaymentKey(String paymentKey) {
        paymentService.findPaymentsByPaymentKey(paymentKey);
    }
}