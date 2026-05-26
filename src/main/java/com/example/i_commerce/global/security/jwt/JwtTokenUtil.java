package com.example.i_commerce.global.security.jwt;

import com.example.i_commerce.domain.member.entity.enums.AdminRole;
import com.example.i_commerce.domain.member.entity.enums.AdminStatus;
import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import com.example.i_commerce.domain.member.entity.enums.SellerStatus;
import com.example.i_commerce.domain.member.tools.AccountRole;
import com.example.i_commerce.domain.member.tools.AccountStatus;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    public String createToken(TokenPayload payload) {

        Instant now = Instant.now();
        Instant expiry = now.plus(1, ChronoUnit.HOURS);

        JwtBuilder builder = Jwts.builder()
            .subject(String.valueOf(payload.accountId()))
            .issuer("i-commerce")
            .claim("principalType", payload.principalType().name())
            .claim("accountId", payload.accountId())
            .claim("role", payload.role().getAuthority())
            .claim("accountStatus", payload.accountStatus().getAuthority())
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(getSigningKey());

        if (payload.sellerStatus() != null) {
            builder.claim("sellerStatus", payload.sellerStatus().getAuthority());
        }

        return builder.compact();
    }

    public TokenPayload parseToken(String token) {
        Claims claims = getClaims(token);

        PrincipalType principalType = PrincipalType.valueOf(
            claims.get("principalType", String.class)
        );

        Long accountId = claims.get("accountId", Long.class);
        String role = claims.get("role", String.class);
        String accountStatus = claims.get("accountStatus", String.class);
        String sellerStatus = claims.get("sellerStatus", String.class);

        AccountRole parsedRole = switch (principalType) {
            case MEMBER -> MemberType.valueOf(role);
            case ADMIN -> AdminRole.valueOf(role);
        };

        AccountStatus parsedAccountStatus = switch (principalType) {
            case MEMBER -> MemberStatus.valueOf(accountStatus);
            case ADMIN -> AdminStatus.valueOf(accountStatus);
        };

        AccountStatus parsedSellerStatus = null;

        if (sellerStatus != null) {
            parsedSellerStatus = SellerStatus.valueOf(sellerStatus);
        }

        return new TokenPayload(
            principalType,
            accountId,
            parsedRole,
            parsedAccountStatus,
            parsedSellerStatus
        );
    }

    public Claims getClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .requireIssuer("i-commerce")
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    public LocalDateTime getExpiration(String token) {
        Claims claims = getClaims(token);

        return claims.getExpiration()
            .toInstant()
            .atZone(ZoneId.of("Asia/Seoul"))
            .toLocalDateTime();
    }
}
