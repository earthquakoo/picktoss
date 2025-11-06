package com.picktoss.picktossserver.core.locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.SimpleTimeZoneAwareLocaleContext;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleContextResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class HeaderLocaleTimeZoneResolver implements LocaleContextResolver {

    private final AcceptHeaderLocaleResolver acceptHeaderLocaleResolver = new AcceptHeaderLocaleResolver();

    public static final String TIMEZONE_HEADER = "X-Timezone";

    @Override
    @NotNull
    public LocaleContext resolveLocaleContext(@NotNull HttpServletRequest request) {
        Locale locale = this.acceptHeaderLocaleResolver.resolveLocale(request);

        TimeZone timeZone = null;
        String timeZoneHeader = request.getHeader(TIMEZONE_HEADER);
        if (StringUtils.hasText(timeZoneHeader)) {
            try {
                timeZone = TimeZone.getTimeZone(timeZoneHeader);
            } catch (Exception e) {
            }
        }

        return new SimpleTimeZoneAwareLocaleContext(locale, timeZone);
    }

    @Override
    public void setLocaleContext(HttpServletRequest request, HttpServletResponse response, LocaleContext localeContext) {
        throw new UnsupportedOperationException("Cannot change locale or timezone - headers are read-only");
    }

    @NotNull
    @Override
    public Locale resolveLocale(@NotNull HttpServletRequest request) {
        return Objects.requireNonNull(resolveLocaleContext(request).getLocale());
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        throw new UnsupportedOperationException("Cannot change locale - headers are read-only");
    }
}
