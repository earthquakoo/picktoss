package com.picktoss.picktossserver.core.locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleTimeZoneAwareLocaleContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleContextResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;
import java.util.TimeZone;

@Component
public class CustomLocaleResolver implements LocaleContextResolver {

    private final AcceptHeaderLocaleResolver acceptHeaderLocaleResolver = new AcceptHeaderLocaleResolver();

    private static final String LOCALE_ATTR_NAME = CustomLocaleResolver.class.getName() + ".LOCALE";

    public static final String LOCALE_HEADER = "X-Locale";
    public static final String TIMEZONE_HEADER = "X-Timezone";

    @Override
    @NotNull
    public LocaleContext resolveLocaleContext(HttpServletRequest request) {
        // 1. Locale 처리 로직
        Locale locale = (Locale) request.getAttribute(LOCALE_ATTR_NAME);

        // 캐싱된 속성이 없으면 헤더 확인 시작
        if (locale == null) {
            String localeHeader = request.getHeader(LOCALE_HEADER);

            if (StringUtils.hasText(localeHeader)) {
                locale = Locale.forLanguageTag(localeHeader);
            }

            if (locale == null || !StringUtils.hasText(locale.getLanguage())) {
                locale = acceptHeaderLocaleResolver.resolveLocale(request);
            }
        }

        TimeZone timeZone = null;
        String timeZoneHeader = request.getHeader(TIMEZONE_HEADER);

        if (StringUtils.hasText(timeZoneHeader)) {
            timeZone = TimeZone.getTimeZone(timeZoneHeader);
        }

        if (timeZone == null) {
            timeZone = TimeZone.getTimeZone("UTC");
        }

        return new SimpleTimeZoneAwareLocaleContext(locale, timeZone);
    }

    @Override
    public void setLocaleContext(HttpServletRequest request, HttpServletResponse response, LocaleContext localeContext) {
        Locale locale = (localeContext != null) ? localeContext.getLocale() : null;
        setLocale(request, response, locale);
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return resolveLocaleContext(request).getLocale();
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        request.setAttribute(LOCALE_ATTR_NAME, locale);
    }
}