//package com.picktoss.picktossserver.core.config;
//
//import com.picktoss.picktossserver.core.locale.LanguageInterceptor;
//import lombok.RequiredArgsConstructor;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
//import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
//
//@Configuration
//@RequiredArgsConstructor
//public class WebConfig implements WebMvcConfigurer {
//
//    private final LanguageInterceptor languageInterceptor;
//    private static final String API_BASE_PATH = "/api/v2/";
//
//    // 인터셉터를 등록
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(languageInterceptor)
//                .addPathPatterns(API_BASE_PATH + "**");  // 모든 경로에 대해 인터셉터를 적용
//    }
//}
