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
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.FormLoginConfigurer;
import org.springframework.security.config.annotation.web.configurers.HttpBasicConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
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

    @Value("${picktoss.server_url}")
    private String picktossServerUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(CsrfConfigurer::disable)
                .cors(corsConfigurer -> corsConfigurer.configurationSource(corsConfigurationSource()))
                .formLogin(FormLoginConfigurer::disable)
                .httpBasic(HttpBasicConfigurer::disable)
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers(
                                "/",
                                "/api/v2/oauth/url",
                                "/api/v2/callback",
                                "swagger-ui/**",
                                "/v3/api-docs/**",
                                "/api/v2/health-check",
                                "/api/v2/login",
                                "/api/v2/backend/login",
                                "/api/v2/example-quizzes",
                                "/api/v2/notion/**",
                                "/api/v2/test/**",
                                "/api/v2/test/create-member",
                                "/api/v2/test/quiz-create"
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
        config.setAllowedOrigins(Arrays.asList(corsAllowedOrigin, corsAllowedOriginDev, picktossServerUrl, corsAllowedOriginProd));
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
