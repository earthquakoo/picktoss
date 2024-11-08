package com.picktoss.picktossserver.domain.auth.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtTokenDto;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.auth.controller.request.LoginRequest;
import com.picktoss.picktossserver.domain.auth.controller.request.SendVerificationCodeRequest;
import com.picktoss.picktossserver.domain.auth.controller.request.VerifyVerificationCodeRequest;
import com.picktoss.picktossserver.domain.auth.controller.response.LoginResponse;
import com.picktoss.picktossserver.domain.auth.facade.AuthFacade;
import com.picktoss.picktossserver.domain.auth.service.AuthService;
import com.picktoss.picktossserver.domain.member.controller.dto.MemberInfoDto;
import com.picktoss.picktossserver.domain.member.facade.MemberFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.*;

@Tag(name = "Auth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class AuthController {

    private final MemberFacade memberFacade;
    private final AuthService authService;
    private final AuthFacade authFacade;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "Oauth url api")
    @GetMapping("/oauth/url")
    public RedirectView oauthUrlApi() {
        String oauthUrl = authService.getRedirectUri();

        return new RedirectView(oauthUrl);
    }

    @Operation(summary = "Oauth callback")
    @GetMapping("/callback")
    public String googleLogin(
            @RequestParam("code") String code
    ) {
        String idToken = authService.getOauthAccessToken(code);
        System.out.println("idToken = " + idToken);

        String decodeJson = authService.decodeIdToken(idToken);
        MemberInfoDto memberInfoDto = authService.transJsonToMemberInfoDto(decodeJson);
        JwtTokenDto jwtTokenDto = memberFacade.createMember(memberInfoDto);
        System.out.println("jwtTokenDto.getAccessToken() = " + jwtTokenDto.getAccessToken());
        return jwtTokenDto.getAccessToken();
    }

    @Operation(summary = "login")
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        LoginResponse response = authFacade.login(request.getAccessToken(), request.getSocialPlatform());
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/auth/verification")
    @ResponseStatus(HttpStatus.OK)
    public void sendVerificationCode(@Valid @RequestBody SendVerificationCodeRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        authFacade.sendVerificationCode(request.getEmail());
    }

    @PostMapping("/auth/verification/check")
    @ApiErrorCodeExamples({MEMBER_NOT_FOUND, EMAIL_VERIFICATION_NOT_FOUND, EMAIL_ALREADY_VERIFIED, INVALID_VERIFICATION_CODE, VERIFICATION_CODE_EXPIRED})
    @ResponseStatus(HttpStatus.OK)
    public void verifyVerificationCode(@Valid @RequestBody VerifyVerificationCodeRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        authFacade.verifyVerificationCode(request.getEmail(), request.getVerificationCode(), memberId);
    }

    @Operation(summary = "Health check")
    @GetMapping("/health-check")
    @ResponseStatus(HttpStatus.OK)
    public String healthCheck() {
        return "I'm doing fine";
    }
}