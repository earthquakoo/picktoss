package com.picktoss.picktossserver.domain.payment.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.payment.dto.request.CreatePaymentsRequest;
import com.picktoss.picktossserver.domain.payment.dto.request.VerifyPaymentRequest;
import com.picktoss.picktossserver.domain.payment.service.PaymentCreateService;
import com.picktoss.picktossserver.domain.payment.service.PaymentVerificationService;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment - Test")
@Controller
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class PaymentController {

    private final JwtTokenProvider jwtTokenProvider;
    private final PaymentCreateService paymentCreateService;
    private final PaymentVerificationService paymentVerificationService;

    @Operation(summary = "포트원 결제 정보 검증")
    @PostMapping("/payments/verify")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<IamportResponse<Payment>> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        IamportResponse<Payment> response = paymentVerificationService.verifyPayment(request.getImpUid(), request.getAmount());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "결제 정보 저장")
    @PostMapping("/payments/save")
    @ResponseStatus(HttpStatus.CREATED)
    public void createPayments(
            @Valid @RequestBody CreatePaymentsRequest request
    ) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        paymentCreateService.createPayments(request.getImpUid(), request.getAmount(), memberId);
    }

    @Operation(summary = "결제 취소하기")
    @PostMapping("/payments/cancel")
    @ResponseStatus(HttpStatus.OK)
    public void cancelPayments() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

    }

    @GetMapping("/front")
    public String hello(Model model){
        return "front";
    }
}