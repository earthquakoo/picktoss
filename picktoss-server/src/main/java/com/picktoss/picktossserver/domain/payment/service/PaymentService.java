package com.picktoss.picktossserver.domain.payment.service;

import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import com.picktoss.picktossserver.domain.payment.entity.Payment;
import com.picktoss.picktossserver.domain.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final MemberService memberService;

    @Transactional
    public void createPayment(String name, Member member) {
        Payment payment = Payment.builder()
                .name(name)
                .member(member)
                .build();
        paymentRepository.save(payment);
    }
}
