package com.example.i_commerce.domain.member.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.security.jwt.JwtTokenUtil;
import com.example.i_commerce.global.security.jwt.RefreshTokenValidator;
import com.example.i_commerce.global.security.jwt.TokenHashEncoder;
import com.example.i_commerce.global.security.jwt.dto.RefreshTokenPayload;
import com.example.i_commerce.global.security.jwt.entity.RefreshToken;
import com.example.i_commerce.global.security.jwt.repo.RefreshTokenRepository;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RefreshTokenValidatorTest {

    private JwtTokenUtil jwtTokenUtil;
    private RefreshTokenRepository refreshTokenRepository;
    private TokenHashEncoder tokenHashEncoder;
    private RefreshTokenValidator refreshTokenValidator;

    @BeforeEach
    void setUp() {
        jwtTokenUtil = Mockito.mock(JwtTokenUtil.class);
        refreshTokenRepository = Mockito.mock(RefreshTokenRepository.class);
        tokenHashEncoder = Mockito.mock(TokenHashEncoder.class);

        refreshTokenValidator = new RefreshTokenValidator(
            jwtTokenUtil,
            refreshTokenRepository,
            tokenHashEncoder
        );
    }

    @Test
    @DisplayName("Refresh Token 검증 성공")
    void validate_success() {
        // given
        String requestToken = "refresh-token";
        String tokenId = "token-id";
        String tokenHash = "hash";

        RefreshTokenPayload payload = new RefreshTokenPayload(
            PrincipalType.MEMBER,
            1L,
            tokenId
        );

        RefreshToken savedToken = RefreshToken.create(
            tokenId,
            PrincipalType.MEMBER,
            1L,
            tokenHash,
            LocalDateTime.now().plusDays(7)
        );

        when(jwtTokenUtil.parseRefreshToken(requestToken)).thenReturn(payload);
        when(refreshTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(savedToken));
        when(tokenHashEncoder.encode(requestToken)).thenReturn(tokenHash);

        // when
        RefreshTokenPayload result = refreshTokenValidator.validate(
            requestToken,
            PrincipalType.MEMBER
        );

        // then
        assertThat(result).isEqualTo(payload);
    }

    @Test
    @DisplayName("기대하는 PrincipalType과 다르면 예외가 발생한다")
    void validate_fail_differentPrincipalType() {
        // given
        String requestToken = "refresh-token";

        RefreshTokenPayload payload = new RefreshTokenPayload(
            PrincipalType.ADMIN,
            1L,
            "token-id"
        );

        when(jwtTokenUtil.parseRefreshToken(requestToken)).thenReturn(payload);

        // when & then
        assertThatThrownBy(() ->
            refreshTokenValidator.validate(requestToken, PrincipalType.MEMBER)
        ).isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("DB에 Refresh Token이 없으면 예외가 발생한다")
    void validate_fail_tokenNotFound() {
        // given
        String requestToken = "refresh-token";
        String tokenId = "token-id";

        RefreshTokenPayload payload = new RefreshTokenPayload(
            PrincipalType.MEMBER,
            1L,
            tokenId
        );

        when(jwtTokenUtil.parseRefreshToken(requestToken)).thenReturn(payload);
        when(refreshTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() ->
            refreshTokenValidator.validate(requestToken, PrincipalType.MEMBER)
        ).isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("요청 Refresh Token hash가 DB hash와 다르면 예외가 발생한다")
    void validate_fail_hashMismatch() {
        // given
        String requestToken = "refresh-token";
        String tokenId = "token-id";

        RefreshTokenPayload payload = new RefreshTokenPayload(
            PrincipalType.MEMBER,
            1L,
            tokenId
        );

        RefreshToken savedToken = RefreshToken.create(
            tokenId,
            PrincipalType.MEMBER,
            1L,
            "saved-hash",
            LocalDateTime.now().plusDays(7)
        );

        when(jwtTokenUtil.parseRefreshToken(requestToken)).thenReturn(payload);
        when(refreshTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(savedToken));
        when(tokenHashEncoder.encode(requestToken)).thenReturn("request-hash");

        // when & then
        assertThatThrownBy(() ->
            refreshTokenValidator.validate(requestToken, PrincipalType.MEMBER)
        ).isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("폐기된 Refresh Token이면 예외가 발생한다")
    void validate_fail_revokedToken() {
        // given
        String requestToken = "refresh-token";
        String tokenId = "token-id";
        String tokenHash = "hash";

        RefreshTokenPayload payload = new RefreshTokenPayload(
            PrincipalType.MEMBER,
            1L,
            tokenId
        );

        RefreshToken savedToken = RefreshToken.create(
            tokenId,
            PrincipalType.MEMBER,
            1L,
            tokenHash,
            LocalDateTime.now().plusDays(7)
        );
        savedToken.revoke(LocalDateTime.now());

        when(jwtTokenUtil.parseRefreshToken(requestToken)).thenReturn(payload);
        when(refreshTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(savedToken));
        when(tokenHashEncoder.encode(requestToken)).thenReturn(tokenHash);

        // when & then
        assertThatThrownBy(() ->
            refreshTokenValidator.validate(requestToken, PrincipalType.MEMBER)
        ).isInstanceOf(AppException.class);
    }

    @Test
    @DisplayName("만료된 Refresh Token이면 예외가 발생한다")
    void validate_fail_expiredToken() {
        // given
        String requestToken = "refresh-token";
        String tokenId = "token-id";
        String tokenHash = "hash";

        RefreshTokenPayload payload = new RefreshTokenPayload(
            PrincipalType.MEMBER,
            1L,
            tokenId
        );

        RefreshToken savedToken = RefreshToken.create(
            tokenId,
            PrincipalType.MEMBER,
            1L,
            tokenHash,
            LocalDateTime.now().minusSeconds(1)
        );

        when(jwtTokenUtil.parseRefreshToken(requestToken)).thenReturn(payload);
        when(refreshTokenRepository.findByTokenId(tokenId)).thenReturn(Optional.of(savedToken));
        when(tokenHashEncoder.encode(requestToken)).thenReturn(tokenHash);

        // when & then
        assertThatThrownBy(() ->
            refreshTokenValidator.validate(requestToken, PrincipalType.MEMBER)
        ).isInstanceOf(AppException.class);
    }
}