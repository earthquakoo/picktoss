package com.picktoss.picktossserver.domain.auth.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.core.jwt.dto.JwtTokenDto;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExample;
import com.picktoss.picktossserver.core.swagger.ApiErrorCodeExamples;
import com.picktoss.picktossserver.domain.auth.dto.request.LoginRequest;
import com.picktoss.picktossserver.domain.auth.dto.request.SendVerificationCodeRequest;
import com.picktoss.picktossserver.domain.auth.dto.request.VerifyInviteCode;
import com.picktoss.picktossserver.domain.auth.dto.request.VerifyVerificationCodeRequest;
import com.picktoss.picktossserver.domain.auth.dto.response.CheckInviteCodeBySignUpResponse;
import com.picktoss.picktossserver.domain.auth.dto.response.CreateInviteLinkResponse;
import com.picktoss.picktossserver.domain.auth.dto.response.LoginResponse;
import com.picktoss.picktossserver.domain.auth.service.AuthCreateService;
import com.picktoss.picktossserver.domain.auth.service.AuthService;
import com.picktoss.picktossserver.domain.member.dto.dto.MemberInfoDto;
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

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthCreateService authCreateService;

    /**
     * GET
     */

    @Operation(summary = "Oauth url api")
    @GetMapping("/oauth/url")
    public RedirectView oauthUrlApi() {
        String oauthUrl = authService.getRedirectUri();

        return new RedirectView(oauthUrl);
    }

    @Operation(summary = "Oauth callback")
    @GetMapping("/callback")
    public String googleLogin(@RequestParam("code") String code) {
        String idToken = authService.getOauthAccessToken(code);
        System.out.println("idToken = " + idToken);

        String decodeJson = authService.decodeIdToken(idToken);
        MemberInfoDto memberInfoDto = authService.transJsonToMemberInfoDto(decodeJson);
        JwtTokenDto jwtTokenDto = authCreateService.createMember(memberInfoDto);
        System.out.println("jwtTokenDto.getAccessToken() = " + jwtTokenDto.getAccessToken());
        return jwtTokenDto.getAccessToken();
    }

    @Operation(summary = "초대 링크 생성")
    @GetMapping("/auth/invite-link")
    @ApiErrorCodeExample(MEMBER_NOT_FOUND)
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<CreateInviteLinkResponse> createInviteLink() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        String inviteLink = authService.createInviteLink(memberId);
        return ResponseEntity.ok().body(new CreateInviteLinkResponse(inviteLink));
    }

    @Operation(summary = "초대 코드로 회원가입했는지 체크")
    @GetMapping("/auth/invite-code/check")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<CheckInviteCodeBySignUpResponse> checkInviteCodeBySignUp() {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        CheckInviteCodeBySignUpResponse response = authService.checkInviteCodeBySignUp(memberId);
        return ResponseEntity.ok().body(response);
    }

    /**
     * POST
     */

    @Operation(summary = "login")
    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request,
            @RequestParam(required = false, value = "invite-link") String inviteLink
    ) {

        LoginResponse response = authService.login(request.getAccessToken(), request.getSocialPlatform(), inviteLink);
        return ResponseEntity.ok().body(response);
    }

    @Operation(summary = "이메일 인증 코드 생성 및 발송")
    @PostMapping("/auth/verification")
    @ResponseStatus(HttpStatus.OK)
    public void sendVerificationCode(@Valid @RequestBody SendVerificationCodeRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        authService.sendVerificationCode(request.getEmail());
    }

    @Operation(summary = "이메일 코드 인증")
    @PostMapping("/auth/verification/check")
    @ApiErrorCodeExamples({MEMBER_NOT_FOUND, EMAIL_VERIFICATION_NOT_FOUND, EMAIL_ALREADY_VERIFIED, INVALID_VERIFICATION_CODE, VERIFICATION_CODE_EXPIRED})
    @ResponseStatus(HttpStatus.OK)
    public void verifyVerificationCode(@Valid @RequestBody VerifyVerificationCodeRequest request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        authService.verifyVerificationCode(request.getEmail(), request.getVerificationCode(), memberId);
    }

    @Operation(summary = "초대 코드 인증")
    @PostMapping("/auth/invite-code/verify")
    @ApiErrorCodeExample(INVITE_LINK_EXPIRED_OR_NOT_FOUND)
    @ResponseStatus(HttpStatus.OK)
    public void verifyInviteCode(@Valid @RequestBody VerifyInviteCode request) {
        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
        Long memberId = jwtUserInfo.getMemberId();

        authService.verifyInviteCode(request.getInviteCode(), memberId);
    }

    /**
     * TEST
     */

    @Operation(summary = "Health check")
    @GetMapping("/health-check")
    @ResponseStatus(HttpStatus.OK)
    public String healthCheck() {
        return "I'm doing fine";
    }
}