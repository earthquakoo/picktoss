package com.picktoss.picktossserver.domain.payment.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.core.redis.RedisUtil;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.payment.controller.dto.TossPaymentResponseDto;
import com.picktoss.picktossserver.domain.payment.entity.TossPayment;
import com.picktoss.picktossserver.domain.payment.repository.PaymentRepository;
import com.picktoss.picktossserver.global.enums.payment.PaymentMethod;
import com.picktoss.picktossserver.global.enums.payment.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentService {

    private final RedisUtil redisUtil;
    private final PaymentRepository paymentRepository;

    @Value("${payment.secret-key}")
    private String tossSecretKey;

    private static final String TOSS_PAYMENT_BASE_URL = "https://api.tosspayments.com/v1/payments/";
    private static final String REDIS_PAYMENT_PREFIX = "payment";

    public void tempSaveAmount(String orderId, Integer amount) {
        redisUtil.setData(REDIS_PAYMENT_PREFIX, orderId, amount, 100000);
    }

    public void verifyAmount(String orderId, Integer amount) {
        Optional<Integer> optionalData = redisUtil.getData(REDIS_PAYMENT_PREFIX, orderId, Integer.class);
        if (optionalData.isEmpty()) {
            throw new CustomException(ErrorInfo.PAYMENT_AMOUNT_ERROR);
        }

        Integer savedAmount = optionalData.get();

        if (!Objects.equals(amount, savedAmount)) {
            throw new CustomException(ErrorInfo.PAYMENT_AMOUNT_ERROR);
        }

        redisUtil.deleteData(REDIS_PAYMENT_PREFIX, orderId);
    }

    @Transactional
    public void confirmPayment(String paymentKey, String orderId, Integer amount, Member member) {
        HttpHeaders httpHeaders = createTossPaymentRequestHeaders();

        HashMap<String, String> params = new HashMap<>();
        params.put("paymentKey", paymentKey);
        params.put("orderId", orderId);
        params.put("amount", amount.toString());

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<HashMap<String, String>> requestHttpEntity = new HttpEntity<>(params, httpHeaders);

        ResponseEntity<TossPaymentResponseDto> responseEntity = restTemplate.postForEntity(TOSS_PAYMENT_BASE_URL + "/confirm", requestHttpEntity, TossPaymentResponseDto.class);
        TossPaymentResponseDto tossPaymentResponseDto = responseEntity.getBody();

        PaymentMethod paymentMethod = PaymentMethod.valueOf("SIMPLE_PAYMENT");
        PaymentStatus paymentStatus = PaymentStatus.valueOf(tossPaymentResponseDto.getPaymentStatus());
        LocalDateTime requestedAt = tossPaymentResponseDto.getRequestAt().toLocalDateTime();
        LocalDateTime approvedAt = tossPaymentResponseDto.getApproveAt().toLocalDateTime();

        TossPayment tossPayment = TossPayment.createTossPayment(paymentKey, orderId, paymentMethod, paymentStatus, amount, requestedAt, approvedAt, member);
        paymentRepository.save(tossPayment);
    }

    public void cancelPayment(String paymentKey) {
        String tossPaymentCancelUrl = TOSS_PAYMENT_BASE_URL + paymentKey + "/cancel";
        HttpHeaders httpHeaders = createTossPaymentRequestHeaders();

        HashMap<String, String> params = new HashMap<>();
        params.put("cancelReason", "구매자 변심");

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<HashMap<String, String>> requestHttpEntity = new HttpEntity<>(params, httpHeaders);

        ResponseEntity<Object> responseEntity = restTemplate.postForEntity(tossPaymentCancelUrl, requestHttpEntity, Object.class);
        System.out.println("responseEntity.getBody(); = " + responseEntity.getBody());
    }

    public void findPaymentsByOrderId(String orderId) {
        String tossPaymentSelectUrl = TOSS_PAYMENT_BASE_URL + "orders/" + orderId;
        HttpHeaders httpHeaders = createTossPaymentRequestHeaders();

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Object> requestHttpEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<Object> responseEntity = restTemplate.postForEntity(tossPaymentSelectUrl, requestHttpEntity, Object.class);
        System.out.println("responseEntity.getBody(); = " + responseEntity.getBody());
    }

    public void findPaymentsByPaymentKey(String paymentKey) {
        String tossPaymentSelectUrl = TOSS_PAYMENT_BASE_URL + paymentKey;
        HttpHeaders httpHeaders = createTossPaymentRequestHeaders();

        RestTemplate restTemplate = new RestTemplate();
        HttpEntity<Object> requestHttpEntity = new HttpEntity<>(httpHeaders);

        ResponseEntity<Object> responseEntity = restTemplate.postForEntity(tossPaymentSelectUrl, requestHttpEntity, Object.class);
        System.out.println("responseEntity.getBody(); = " + responseEntity.getBody());
    }

    private HttpHeaders createTossPaymentRequestHeaders() {
        String authorization = Base64.getEncoder().encodeToString((tossSecretKey + ":").getBytes(StandardCharsets.UTF_8));

        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set("Authorization", "Basic " + authorization);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);

        return httpHeaders;
    }
}
