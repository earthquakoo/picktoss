package com.picktoss.picktossserver.domain.payment.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.payment.controller.request.CreatePaymentRequest;
import com.picktoss.picktossserver.domain.payment.facade.PaymentFacade;
import com.picktoss.picktossserver.domain.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class PaymentController {

    private final JwtTokenProvider jwtTokenProvider;
    private final PaymentFacade paymentFacade;

    @PostMapping("/payment")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();
        paymentFacade.createPayment(request.getName(), memberId);
    }
}
