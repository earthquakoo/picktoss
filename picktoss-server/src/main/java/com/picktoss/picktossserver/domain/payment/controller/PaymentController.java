package com.picktoss.picktossserver.domain.payment.controller;

import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.domain.payment.controller.request.CancelPaymentRequest;
import com.picktoss.picktossserver.domain.payment.controller.request.SaveAmountRequest;
import com.picktoss.picktossserver.domain.payment.controller.request.TossPaymentRequest;
import com.picktoss.picktossserver.domain.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class PaymentController {

    private final JwtTokenProvider jwtTokenProvider;
    private final PaymentService paymentService;

    @Operation(summary = "주문과 결제금액 임시저장")
    @PostMapping("/payments/temp-save")
    @ApiErrorCodeExample(ErrorInfo.PAYMENT_AMOUNT_ERROR)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> tempSaveAmount(@Valid @RequestBody SaveAmountRequest saveAmountRequest) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        paymentService.tempSaveAmount(saveAmountRequest.getOrderId(), saveAmountRequest.getAmount());
        return ResponseEntity.ok("Payment temp save successful");
    }

    @Operation(summary = "결제금액 확인")
    @PostMapping("/payments/verify-amount")
    @ApiErrorCodeExample(ErrorInfo.PAYMENT_AMOUNT_ERROR)
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<?> verifyAmount(@Valid @RequestBody SaveAmountRequest saveAmountRequest) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        paymentService.verifyAmount(saveAmountRequest.getOrderId(), saveAmountRequest.getAmount());
        return ResponseEntity.ok("Payment is valid");
    }

    @Operation(summary = "결제 승인 요청")
    @PostMapping("/payments/confirm")
    @ResponseStatus(HttpStatus.OK)
    public void confirmPayment(@Valid @RequestBody TossPaymentRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        paymentService.confirmPayment(request.getPaymentKey(), request.getOrderId(), request.getAmount(), memberId);
    }

    @Operation(summary = "결제 취소 요청")
    @PostMapping("/payments/cancel")
    @ResponseStatus(HttpStatus.OK)
    public void cancelPayment(@Valid @RequestBody CancelPaymentRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        paymentService.cancelPayment(request.getPaymentKey());
    }

    @Operation(summary = "orderId로 결제 조회")
    @GetMapping("/payments/orders/{order_id}")
    @ResponseStatus(HttpStatus.OK)
    public void getPaymentByOrderId(@PathVariable("order_id") String orderId) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        paymentService.findPaymentsByOrderId(orderId);
    }

    @Operation(summary = "paymentKey로 결제 조회")
    @GetMapping("/payments/{payment_key}")
    @ResponseStatus(HttpStatus.OK)
    public void getPaymentByPaymentKey(@PathVariable("payment_key") String paymentKey) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        paymentService.findPaymentsByPaymentKey(paymentKey);
    }
}
