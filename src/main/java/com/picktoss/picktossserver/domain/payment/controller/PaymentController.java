package com.picktoss.picktossserver.domain.payment.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.payment.controller.request.CreatePaymentRequest;
import com.picktoss.picktossserver.domain.payment.facade.PaymentFacade;
import com.picktoss.picktossserver.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PaymentController {

    private final JwtTokenProvider jwtTokenProvider;
    private final PaymentFacade paymentFacade;

    @PostMapping("/payment")
    public void createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        String memberId = jwtUserInfo.getMemberId();
        paymentFacade.createPayment(request.getName(), memberId);
    }
}
