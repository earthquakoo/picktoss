package com.picktoss.picktossserver.core.locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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

        if (uri.startsWith(API_BASE_PATH)) {
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
                    localeResolver.setLocale(request, response, locale);

                    String newUri = API_BASE_PATH + path.substring(lang.length() + 1);

                    request.getRequestDispatcher(newUri).forward(request, response);
                    return false;
                }
            }
        }

        localeResolver.setLocale(request, response, Locale.ENGLISH);
        return true;
    }
}
