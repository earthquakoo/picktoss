package com.picktoss.picktossserver.domain.auth.facade;

import com.picktoss.picktossserver.domain.auth.service.AuthService;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AuthFacade {

    private final AuthService authService;
    private final MemberService memberService;

    @Transactional
    public void sendVerificationCode(String email) {
        authService.sendVerificationCode(email);
    }

    @Transactional
    public void verifyVerificationCode(String email, String verificationCode) {
        authService.verifyVerificationCode(email, verificationCode);
    }
}
