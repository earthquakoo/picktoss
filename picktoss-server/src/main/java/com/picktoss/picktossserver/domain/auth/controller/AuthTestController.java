package com.picktoss.picktossserver.domain.auth.controller;

import com.picktoss.picktossserver.core.jwt.dto.JwtTokenDto;
import com.picktoss.picktossserver.domain.auth.dto.KakaoMemberDto;
import com.picktoss.picktossserver.domain.auth.service.AuthCreateService;
import com.picktoss.picktossserver.domain.auth.service.AuthTestService;
import com.picktoss.picktossserver.domain.auth.util.AuthUtil;
import com.picktoss.picktossserver.domain.member.dto.dto.MemberInfoDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

@Tag(name = "Auth - Test")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class AuthTestController {

    private final AuthCreateService authCreateService;
    private final AuthTestService authTestService;
    private final AuthUtil authUtil;

    @Operation(summary = "Google Oauth url api")
    @GetMapping("/oauth/url")
    public RedirectView oauthUrlApi() {
        String oauthUrl = authTestService.getRedirectUri();

        return new RedirectView(oauthUrl);
    }

    @Operation(summary = "Google Oauth callback")
    @GetMapping("/callback")
    public String googleLogin(@RequestParam("code") String code) {
        String idToken = authTestService.getOauthAccessToken(code);
        System.out.println("idToken = " + idToken);

        String decodeJson = authUtil.decodeIdToken(idToken);
        MemberInfoDto memberInfoDto = authUtil.transJsonToMemberInfoDto(decodeJson);
        JwtTokenDto jwtTokenDto = authTestService.createMember(memberInfoDto);
        System.out.println("jwtTokenDto.getAccessToken() = " + jwtTokenDto.getAccessToken());
        return jwtTokenDto.getAccessToken();
    }

    @Operation(summary = "Kakao Oauth url api")
    @GetMapping("/oauth/url/kakao")
    public RedirectView kakaoOauthUrlApi() {
        String kakaoRedirectUri = authTestService.getKakaoRedirectUri();
        return new RedirectView(kakaoRedirectUri);
    }

    @Operation(summary = "Kakao Oauth callback")
    @GetMapping("/kakao/callback")
    public void kakaoLogin(@RequestParam("code") String code) {
        String kakaoOauthAccessToken = authTestService.getKakaoOauthAccessToken(code);
        System.out.println("kakaoOauthAccessToken = " + kakaoOauthAccessToken);
    }

    @Operation(summary = "Get Kakao user info")
    @GetMapping("/kakao/info")
    public void getKakaoUserInfo(@RequestParam("code") String code) {
        String oauthAccessMemberInfo = authTestService.getOauthAccessMemberInfo(code);
        System.out.println("oauthAccessMemberInfo = " + oauthAccessMemberInfo);
        KakaoMemberDto kakaoMemberDto = authUtil.transJsonToKakaoMemberDto(oauthAccessMemberInfo);
        String email = kakaoMemberDto.getKakaoAccount().getEmail();
        String nickname = kakaoMemberDto.getKakaoAccount().getProfile().getNickName();

        System.out.println("email = " + email);
        System.out.println("nickname = " + nickname);
    }
}