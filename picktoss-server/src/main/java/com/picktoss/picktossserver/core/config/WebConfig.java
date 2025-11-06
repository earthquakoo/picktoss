package com.picktoss.picktossserver.core.config;

import com.picktoss.picktossserver.core.locale.HeaderLocaleTimeZoneResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public LocaleResolver localeResolver() {
        return new HeaderLocaleTimeZoneResolver();
    }
}
