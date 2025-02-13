package com.picktoss.picktossserver.domain.auth.service;

import com.picktoss.picktossserver.core.email.MailgunEmailSenderManager;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.domain.auth.entity.EmailVerification;
import com.picktoss.picktossserver.domain.auth.repository.EmailVerificationRepository;
import com.picktoss.picktossserver.domain.auth.util.AuthUtil;
import com.picktoss.picktossserver.domain.member.entity.Member;
import com.picktoss.picktossserver.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthEmailVerificationService {

    private final MailgunEmailSenderManager mailgunEmailSenderManager;
    private final EmailVerificationRepository emailVerificationRepository;
    private final MemberRepository memberRepository;
    private final AuthUtil authUtil;

    @Value("${email_verification.expire_seconds}")
    private long verificationExpireDurationSeconds;

    /**
     * 이메일 인증코드 발송
     */
    @Transactional
    public void sendVerificationCode(String email) {
        // Send verification code
        String verificationCode = authUtil.generateUniqueCode();
        mailgunEmailSenderManager.sendVerificationCode(email, verificationCode);

        // Upsert register verification entry (always make is_verified to False)
        Optional<EmailVerification> optionalEmailVerification = emailVerificationRepository.findByEmail(email);
        if (optionalEmailVerification.isPresent()) {
            EmailVerification emailVerification = optionalEmailVerification.get();

            emailVerification.updateVerificationCode(verificationCode);
            emailVerification.unverify();
            emailVerification.renewExpirationTimeFromNow(verificationExpireDurationSeconds);
        } else {
            EmailVerification newEmailVerification = EmailVerification.builder()
                    .email(email)
                    .verificationCode(verificationCode)
                    .isVerified(false)
                    .expirationTime(LocalDateTime.now().plusSeconds(verificationExpireDurationSeconds))
                    .build();

            emailVerificationRepository.save(newEmailVerification);
        }
    }

    /**
     * 이메일 인증코드 인증
     */
    @Transactional
    public void verifyVerificationCode(String email, String verificationCode, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(MEMBER_NOT_FOUND));

        Optional<EmailVerification> optionalEmailVerification = emailVerificationRepository.findByEmail(email);
        if (optionalEmailVerification.isEmpty()) {
            throw new CustomException(EMAIL_VERIFICATION_NOT_FOUND);
        }

        EmailVerification emailVerification = optionalEmailVerification.get();

        if (emailVerification.isVerified()) {
            throw new CustomException(EMAIL_ALREADY_VERIFIED);
        }

        if (!emailVerification.getVerificationCode().equals(verificationCode)) {
            throw new CustomException(INVALID_VERIFICATION_CODE);
        }

        if (emailVerification.isExpired()) {
            throw new CustomException(VERIFICATION_CODE_EXPIRED);
        }
        emailVerification.verify();
        member.updateMemberEmail(email);
    }
}
