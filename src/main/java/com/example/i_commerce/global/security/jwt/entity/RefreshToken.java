package com.example.i_commerce.global.security.jwt.entity;

import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.Getter;

@Entity
@Getter
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token_id", nullable = false, unique = true, length = 36)
    private String tokenId;

    @Enumerated(EnumType.STRING)
    @Column(name = "principal_type", nullable = false, length = 20)
    private PrincipalType principalType;

    @Column(name = "account_id", nullable = false)
    private Long accountId;

    @Column(name = "refresh_token_hash", nullable = false, length = 100)
    private String refreshTokenHash;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    public static RefreshToken create(
        String tokenId,
        PrincipalType principalType,
        Long accountId,
        String refreshTokenHash,
        LocalDateTime expiresAt
    ) {
        RefreshToken token = new RefreshToken();
        token.tokenId = tokenId;
        token.principalType = principalType;
        token.accountId = accountId;
        token.refreshTokenHash = refreshTokenHash;
        token.expiresAt = expiresAt;
        return token;
    }

    public boolean matches(String refreshTokenHash) {
        return this.refreshTokenHash.equals(refreshTokenHash);
    }

    public boolean isExpired(LocalDateTime now) {
        return !expiresAt.isAfter(now);
    }

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public void revoke(LocalDateTime now) {
        this.revokedAt = now;
    }
}
