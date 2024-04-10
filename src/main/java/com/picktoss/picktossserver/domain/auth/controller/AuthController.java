package com.picktoss.picktossserver.domain.auth.controller;

import com.picktoss.picktossserver.core.jwt.dto.JwtTokenDto;
import com.picktoss.picktossserver.domain.auth.service.AuthService;
import com.picktoss.picktossserver.domain.member.controller.dto.MemberInfoDto;
import com.picktoss.picktossserver.domain.member.facade.MemberFacade;
import com.picktoss.picktossserver.domain.member.service.MemberService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
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

    @SneakyThrows
    @GetMapping("/oauth/url")
    public HashMap<String, String> oauthUrlApi(HttpServletResponse response) {
        return authService.getRedirectUri();
    }

//    @SneakyThrows
//    @GetMapping("/oauth/url")
//    public String oauthUrlApi(HttpServletResponse response) {
//        String redirectUri = authService.getRedirectUri();
//        response.sendRedirect(redirectUri);
//        return redirectUri;
//    }

    @GetMapping("/callback")
    @ResponseStatus(HttpStatus.OK)
    public RedirectView googleLogin(@RequestParam("code") String code, RedirectAttributes redirectAttributes) {
        String idToken = authService.getOauthAccessToken(code);
        System.out.println("idToken = " + idToken);

        String decodeJson = authService.decodeIdToken(idToken);
        MemberInfoDto memberInfoDto = authService.transJsonToMemberInfoDto(decodeJson);
        JwtTokenDto jwtTokenDto = memberFacade.createMember(memberInfoDto);

        redirectAttributes.addAttribute("access-token", jwtTokenDto.getAccessToken());

        System.out.println("jwtTokenDto.getAccessToken() = " + jwtTokenDto.getAccessToken());
        return new RedirectView("http://localhost:5173" + "/oauth");
    }

    @GetMapping("/health-check")
    @ResponseStatus(HttpStatus.OK)
    public String healthCheck() {
        return "I'm doing fine";
    }

}
