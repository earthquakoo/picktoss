package com.picktoss.picktossserver.domain.payment.service;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorInfo;
import com.picktoss.picktossserver.domain.payment.constant.PaymentConstant;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.response.IamportResponse;
import com.siot.IamportRestClient.response.Payment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentVerificationService {

    private final IamportClient iamportClient;

    // 포트원 서버로부터 결제 정보 검증
    public IamportResponse<Payment> verifyPayment(String impUid, int amount) {
        try {
            IamportResponse<Payment> iamportResponse = iamportClient.paymentByImpUid(impUid);
            int iamportAmount = iamportResponse.getResponse().getAmount().intValue();

            if (!iamportResponse.getResponse().getStatus().equals("paid")) {
                throw new CustomException(ErrorInfo.PAYMENT_NOT_COMPLETED);
            }

            if (iamportAmount != amount) {
                throw new CustomException(ErrorInfo.PAYMENT_AMOUNT_DIFFERENT_FROM_IAMPORT_SERVER);
            } else if (iamportAmount != PaymentConstant.SUBSCRIPTION_PAYMENT_AMOUNT) {
                throw new CustomException(ErrorInfo.PAYMENT_AMOUNT_DIFFERENT_FROM_DB);
            }

            return iamportResponse;
        } catch (Exception e) {
            log.info(e.getMessage());
            return null;
        }
    }
}
