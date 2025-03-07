package com.picktoss.picktossserver.core.jwt;

import com.picktoss.picktossserver.core.exception.CustomException;
import com.picktoss.picktossserver.core.jwt.dto.JwtTokenDto;
import com.picktoss.picktossserver.core.jwt.dto.JwtUserInfo;
import com.picktoss.picktossserver.domain.admin.entity.Admin;
import com.picktoss.picktossserver.domain.member.entity.Member;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

import static com.picktoss.picktossserver.core.exception.ErrorInfo.EXPIRED_JWT_TOKEN;
import static com.picktoss.picktossserver.core.exception.ErrorInfo.INVALID_JWT_TOKEN;

@Component
public class JwtTokenProvider {

    private final Key key;

    @Value("${jwt.access_token_expiration_ms}")
    private long accessTokenExpirationTimeMs;

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public JwtTokenDto generateToken(Member member) {
        Date accessTokenExpiration = getTokenExpiration(accessTokenExpirationTimeMs);

        String accessToken = Jwts.builder()
                .setSubject(member.getId().toString())
                .setExpiration(accessTokenExpiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .claim("role", member.getRole().name())
                .compact();

        return JwtTokenDto.builder()
                .accessToken(accessToken)
                .accessTokenExpiration(accessTokenExpiration)
                .build();
    }

    public JwtTokenDto generateAdminToken(Admin admin) {
        Date accessTokenExpiration = getTokenExpiration(accessTokenExpirationTimeMs);

        String accessToken = Jwts.builder()
                .setSubject(admin.getId().toString())
                .setExpiration(accessTokenExpiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .claim("role", "ROLE_ADMIN")
                .compact();

        return JwtTokenDto.builder()
                .accessToken(accessToken)
                .accessTokenExpiration(accessTokenExpiration)
                .build();
    }

    public Date getTokenExpiration(long expirationMs) {
        long now = (new Date()).getTime();
        return new Date(now + expirationMs);
    }

    public JwtUserInfo validateAndExtractUserInfo(String token) {
        try {
            Jws<Claims> parsedToken = Jwts
                    .parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            Long memberId = Long.parseLong(parsedToken.getBody().getSubject());
            String role = parsedToken.getBody().get("role", String.class);

            return new JwtUserInfo(memberId, role);
        } catch (SecurityException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            throw new CustomException(INVALID_JWT_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new CustomException(EXPIRED_JWT_TOKEN);
        }
    }

    public JwtUserInfo getCurrentUserInfo() {
        return (JwtUserInfo) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
