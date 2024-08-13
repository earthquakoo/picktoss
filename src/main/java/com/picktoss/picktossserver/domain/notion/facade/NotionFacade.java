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

    public String getNotionRedirectUri() {
        return notionService.getNotionRedirectUri();
    }

    public String getNotionOauthAccessToken(String accessCode) {
        return notionService.getNotionOauthAccessToken(accessCode);
    }

    public String getNotionPages(String accessToken) {
        return notionService.getNotionPages(accessToken);
    }

    public String getNotionPage(String accessToken) {
        return notionService.getNotionPage(accessToken);
    }
}
