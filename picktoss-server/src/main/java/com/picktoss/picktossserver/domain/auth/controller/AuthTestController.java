package com.picktoss.picktossserver.domain.auth.controller;

import com.picktoss.picktossserver.core.jwt.dto.JwtTokenDto;
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

    @Operation(summary = "Oauth url api")
    @GetMapping("/oauth/url")
    public RedirectView oauthUrlApi() {
        String oauthUrl = authTestService.getRedirectUri();

        return new RedirectView(oauthUrl);
    }

    @Operation(summary = "Oauth callback")
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
}
