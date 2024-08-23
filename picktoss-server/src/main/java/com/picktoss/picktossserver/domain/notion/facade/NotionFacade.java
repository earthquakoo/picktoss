package com.picktoss.picktossserver.domain.notion.facade;

import com.picktoss.picktossserver.domain.notion.service.NotionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotionFacade {

    private final NotionService notionService;

    public String findNotionRedirectUri() {
        return notionService.findNotionRedirectUri();
    }

    public String findNotionOauthAccessToken(String accessCode) {
        return notionService.findNotionOauthAccessToken(accessCode);
    }

    public String findNotionPages(String accessToken) {
        return notionService.findNotionPages(accessToken);
    }

    public String findNotionPage(String accessToken) {
        return notionService.findNotionPage(accessToken);
    }
}
