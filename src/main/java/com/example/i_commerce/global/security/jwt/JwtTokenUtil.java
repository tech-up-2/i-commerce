package com.example.i_commerce.global.security.jwt;

import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.tools.AccountRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenUtil {

    @Value("${app.jwt.secret}")
    private String secretKey;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Base64.getDecoder().decode(secretKey));
    }

    public String createToken(Long memberId, String email, AccountRole role, MemberStatus status) {

        Instant now = Instant.now();
        Instant expiry = now.plus(1, ChronoUnit.HOURS);

        return Jwts.builder() //현재 알고리즘으로 HS256을 사용중인데 나중에 RS256으로 전환해야함(보안성 향상 위해)
            .subject(String.valueOf(memberId))
            .issuer("i-commerce")
            .claim("email", email)
            .claim("role", role.name())
            .claim("status", status.name())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(getSigningKey())
            .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}
