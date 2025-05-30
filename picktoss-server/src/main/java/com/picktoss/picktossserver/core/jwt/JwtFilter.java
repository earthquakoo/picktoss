package com.picktoss.picktossserver.core.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.exception.ErrorResponseDto;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        /** [1] Extract token from authorization header **/
        // Extract authorization header
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract token
        String token = authorizationHeader.split(" ")[1];

        try {
            /** [2] Validate Token & Extract user information **/
            JwtUserInfo jwtUserInfo = jwtTokenProvider.validateAndExtractUserInfo(token);

            /** [3] Set Security Context **/
            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                    jwtUserInfo,
                    null,
                    List.of(new SimpleGrantedAuthority(jwtUserInfo.getRole()))
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (CustomException e) {
            // Handle custom exception
            ErrorResponseDto errorResponseDto = new ErrorResponseDto(
                    e.getErrorInfo().getStatusCode(),
                    e.getErrorInfo().getErrorCode(),
                    e.getMessage()
            );
            response.setStatus(errorResponseDto.getStatusCode());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writeValue(response.getWriter(), errorResponseDto);
        }
    }
}
