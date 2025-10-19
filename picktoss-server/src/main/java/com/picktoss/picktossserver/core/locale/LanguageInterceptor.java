package com.picktoss.picktossserver.core.locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Component
@RequiredArgsConstructor
public class LanguageInterceptor implements HandlerInterceptor {

    private final LocaleResolver localeResolver;
    private static final String API_BASE_PATH = "/api/v2/";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getAttribute("jakarta.servlet.forward.request_uri") != null) {
            return true;
        }

        String uri = request.getRequestURI();

        // 요청 URI가 /api/v2/ 로 시작하는지 확인
        if (uri.startsWith(API_BASE_PATH)) {
            System.out.println("uri = " + uri);
            // /api/v2/ 를 제외한 나머지 경로 추출 (예: en/documents)
            String path = uri.substring(API_BASE_PATH.length());
            String[] parts = path.split("/");

            if (parts.length > 0) {
                String lang = parts[0];
                Locale locale = null;

                if ("en".equalsIgnoreCase(lang)) {
                    System.out.println("English");
                    locale = Locale.ENGLISH;
                } else if ("ko".equalsIgnoreCase(lang)) {
                    locale = Locale.KOREAN;
                    System.out.println("Korean");
                }

                if (locale != null) {
                    System.out.println("Support Language");
                    localeResolver.setLocale(request, response, locale);

                    String newUri = API_BASE_PATH + path.substring(lang.length() + 1);

                    request.getRequestDispatcher(newUri).forward(request, response);
                    return false;
                }
            }
            System.out.println("Exception");
        }

        // 언어 코드가 없는 경우, 기본값(한국어)으로 설정하고 계속 진행
        localeResolver.setLocale(request, response, Locale.KOREAN);
        return true;
    }
}
