package com.example.i_commerce.domain.member.entity;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.i_commerce.global.security.jwt.entity.RefreshToken;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

    @Test
    @DisplayName("RefreshToken 생성 성공")
    void create_success() {
        // given
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        // when
        RefreshToken refreshToken = RefreshToken.create(
            "token-id",
            PrincipalType.MEMBER,
            1L,
            "refresh-token-hash",
            expiresAt
        );

        // then
        assertThat(refreshToken.getTokenId()).isEqualTo("token-id");
        assertThat(refreshToken.getPrincipalType()).isEqualTo(PrincipalType.MEMBER);
        assertThat(refreshToken.getAccountId()).isEqualTo(1L);
        assertThat(refreshToken.getRefreshTokenHash()).isEqualTo("refresh-token-hash");
        assertThat(refreshToken.getExpiresAt()).isEqualTo(expiresAt);
        assertThat(refreshToken.getRevokedAt()).isNull();
    }

    @Test
    @DisplayName("RefreshToken hash가 일치하면 true를 반환한다")
    void matches_true() {
        // given
        RefreshToken refreshToken = RefreshToken.create(
            "token-id",
            PrincipalType.MEMBER,
            1L,
            "hash",
            LocalDateTime.now().plusDays(7)
        );

        // when & then
        assertThat(refreshToken.matches("hash")).isTrue();
    }

    @Test
    @DisplayName("RefreshToken hash가 일치하지 않으면 false를 반환한다")
    void matches_false() {
        // given
        RefreshToken refreshToken = RefreshToken.create(
            "token-id",
            PrincipalType.MEMBER,
            1L,
            "hash",
            LocalDateTime.now().plusDays(7)
        );

        // when & then
        assertThat(refreshToken.matches("other-hash")).isFalse();
    }

    @Test
    @DisplayName("만료 시간이 현재보다 이전이면 만료된 토큰이다")
    void isExpired_true() {
        // given
        LocalDateTime now = LocalDateTime.now();

        RefreshToken refreshToken = RefreshToken.create(
            "token-id",
            PrincipalType.MEMBER,
            1L,
            "hash",
            now.minusSeconds(1)
        );

        // when & then
        assertThat(refreshToken.isExpired(now)).isTrue();
    }

    @Test
    @DisplayName("만료 시간이 현재보다 이후면 만료되지 않은 토큰이다")
    void isExpired_false() {
        // given
        LocalDateTime now = LocalDateTime.now();

        RefreshToken refreshToken = RefreshToken.create(
            "token-id",
            PrincipalType.MEMBER,
            1L,
            "hash",
            now.plusSeconds(1)
        );

        // when & then
        assertThat(refreshToken.isExpired(now)).isFalse();
    }

    @Test
    @DisplayName("토큰을 폐기하면 revokedAt이 저장된다")
    void revoke_success() {
        // given
        LocalDateTime now = LocalDateTime.now();

        RefreshToken refreshToken = RefreshToken.create(
            "token-id",
            PrincipalType.MEMBER,
            1L,
            "hash",
            now.plusDays(7)
        );

        // when
        refreshToken.revoke(now);

        // then
        assertThat(refreshToken.isRevoked()).isTrue();
        assertThat(refreshToken.getRevokedAt()).isEqualTo(now);
    }
}