package com.picktoss.picktossserver.core.config;

import com.picktoss.picktossserver.core.jwt.JwtAccessDeniedHandler;
import com.picktoss.picktossserver.core.jwt.JwtAuthenticationEntryPoint;
import com.picktoss.picktossserver.core.jwt.JwtFilter;
import com.picktoss.picktossserver.core.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Value("${cors.cors_allowed_origin}")
    private String corsAllowedOrigin;

    @Value("${cors.cors_allowed_origin_dev}")
    private String corsAllowedOriginDev;

    @Value("${cors.cors_allowed_origin_prod}")
    private String corsAllowedOriginProd;

    @Value("${cors.cors_allowed_origin_backoffice}")
    private String corsAllowedOriginBackoffice;

    @Value("${cors.cors_allowed_origin_localization}")
    private String corsAllowedOriginLocalization;

    @Value("${picktoss.server_url}")
    private String picktossServerUrl;

    private static final String corsAllowedOriginAppUrl = "https://app.picktoss.com/";
    private static final String corsAllowedOriginVercel = "https://picktoss-explore-detail.vercel.app/";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(CsrfConfigurer::disable)
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))
                .formLogin(FormLoginConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .anonymous(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(new AntPathRequestMatcher("/api/v2/documents/{document_id:[0-9]+}"))
                        .permitAll()
                        .requestMatchers(
                                "/",
                                "/api/v2/oauth/url",
                                "/api/v2/callback",
                                "/api/v2/oauth/url/kakao",
                                "/api/v2/kakao/info",
                                "/api/v2/kakao/callback",
                                "swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/v2/health-check",
                                "/api/v2/login",
                                "/api/v2/test/**",
                                "/api/v2/admin/login",
                                "/api/v2/admin/sign-up",
                                "/api/v2/auth/invite/{invite_code}/creator",
                                "/api/v2/auth/invite/verify",
                                "/api/v2/categories",
                                "/api/v2/documents/public",
                                "/api/v2/documents/{document_id}/public",
                                "/api/v2/documents/public/search",
                                "/api/v2/documents/{document_id}/quiz-sets",
                                "/api/v2/quiz-sets/{quiz_set_id}"
                        )
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling((it) -> {
                    it.authenticationEntryPoint(new JwtAuthenticationEntryPoint());
                    it.accessDeniedHandler(new JwtAccessDeniedHandler());
                })
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtFilter(jwtTokenProvider), UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

//        config.addAllowedOrigin(corsAllowedOrigin);
        config.setAllowedOrigins(Arrays.asList(
                corsAllowedOrigin,
                corsAllowedOriginDev,
                picktossServerUrl,
                corsAllowedOriginProd,
                corsAllowedOriginBackoffice,
                corsAllowedOriginLocalization,
                corsAllowedOriginAppUrl,
                corsAllowedOriginVercel
        ));
        config.addAllowedOriginPattern("*");
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
