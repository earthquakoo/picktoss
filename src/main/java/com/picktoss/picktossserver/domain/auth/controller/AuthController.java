package com.picktoss.picktossserver.domain.auth.controller;

import com.picktoss.picktossserver.core.jwt.dto.JwtTokenDto;
import com.picktoss.picktossserver.domain.auth.service.AuthService;
import com.picktoss.picktossserver.domain.member.controller.dto.MemberInfoDto;
import com.picktoss.picktossserver.domain.member.facade.MemberFacade;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.util.HashMap;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class AuthController {

    private final MemberFacade memberFacade;
    private final AuthService authService;

    @Operation(summary = "Oauth url api")
    @GetMapping("/oauth/url")
    public HashMap<String, String> oauthUrlApi() {
        return authService.getRedirectUri();
    }

//    @GetMapping("/oauth/url")
//    public String oauthUrlApi(HttpServletResponse response) {
//        String redirectUri = authService.getRedirectUri();
//        response.sendRedirect(redirectUri);
//        return redirectUri;
//    }

    @Operation(summary = "Oauth callback")
    @GetMapping("/callback")
    public RedirectView googleLogin(
            @RequestParam("code") String code,
            RedirectAttributes redirectAttributes
    ) {
        String idToken = authService.getOauthAccessToken(code);
        System.out.println("idToken = " + idToken);

        String decodeJson = authService.decodeIdToken(idToken);
        MemberInfoDto memberInfoDto = authService.transJsonToMemberInfoDto(decodeJson);
        JwtTokenDto jwtTokenDto = memberFacade.createMember(memberInfoDto);
        System.out.println("jwtTokenDto.getAccessToken() = " + jwtTokenDto.getAccessToken());

        String oauthCallbackResponseUrl = authService.getOauthCallbackResponseUrl();

        redirectAttributes.addAttribute("access-token", jwtTokenDto.getAccessToken());
        return new RedirectView(oauthCallbackResponseUrl);
    }

    @Operation(summary = "Health check")
    @GetMapping("/health-check")
    @ResponseStatus(HttpStatus.OK)
    public String healthCheck() {
        return "I'm doing fine";
    }

}
