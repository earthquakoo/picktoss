package com.picktoss.picktossserver.domain.notion.controller;

import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import com.picktoss.picktossserver.domain.notion.facade.NotionFacade;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Tag(name = "Notion")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2")
public class NotionController {

    private final NotionFacade notionFacade;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/notion/oauth")
    @ResponseStatus(HttpStatus.OK)
    public void verifyNotion(HttpServletResponse response) throws IOException {
        String notionRedirectUri = notionFacade.findNotionRedirectUri();
        response.sendRedirect(notionRedirectUri);
    }

    @GetMapping("/notion/callback")
    @ResponseStatus(HttpStatus.OK)
    public String notionCallback(
            @RequestParam("code") String code
    ) {
        return notionFacade.findNotionOauthAccessToken(code);
    }

    @Operation(summary = "Get notion pages")
    @GetMapping("/notion/pages")
    @ResponseStatus(HttpStatus.OK)
    public String getNotionPages() {
//        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
//        Long memberId = jwtUserInfo.getMemberId();
        String accessToken = "";
        return notionFacade.findNotionPages(accessToken);
    }

    @Operation(summary = "Get notion page")
    @GetMapping("/notion/page/{page_id}")
    @ResponseStatus(HttpStatus.OK)
    public String getNotionPage(
            @PathVariable("page_id") String pageId
    ) {
//        JwtUserInfo jwtUserInfo = jwtTokenProvider.getCurrentUserInfo();
//        Long memberId = jwtUserInfo.getMemberId();
        String accessToken = "";
        return notionFacade.findNotionPage(accessToken, pageId);
    }
}
