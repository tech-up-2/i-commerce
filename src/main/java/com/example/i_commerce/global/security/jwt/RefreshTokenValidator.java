package com.example.i_commerce.global.security.jwt;

import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.security.jwt.dto.RefreshTokenPayload;
import com.example.i_commerce.global.security.jwt.entity.RefreshToken;
import com.example.i_commerce.global.security.jwt.repo.RefreshTokenRepository;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RefreshTokenValidator {

    private final JwtTokenUtil jwtTokenUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenHashEncoder tokenHashEncoder;

    @Transactional(readOnly = true)
    public RefreshTokenPayload validate(
        String refreshToken,
        PrincipalType expectedPrincipalType
    ) {
        RefreshToken savedToken = validateAndGetToken(
            refreshToken,
            expectedPrincipalType
        );

        return new RefreshTokenPayload(
            savedToken.getPrincipalType(),
            savedToken.getAccountId(),
            savedToken.getTokenId()
        );
    }

    @Transactional(readOnly = true)
    public RefreshToken validateAndGetToken(
        String refreshToken,
        PrincipalType expectedPrincipalType
    ) {
        RefreshTokenPayload payload = jwtTokenUtil.parseRefreshToken(refreshToken);

        if (payload.principalType() != expectedPrincipalType) {
            throw new AppException(MemberErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshToken savedToken = refreshTokenRepository.findByTokenId(payload.tokenId())
            .orElseThrow(() -> new AppException(MemberErrorCode.INVALID_REFRESH_TOKEN));

        if (savedToken.getPrincipalType() != expectedPrincipalType) {
            throw new AppException(MemberErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (!savedToken.getAccountId().equals(payload.accountId())) {
            throw new AppException(MemberErrorCode.INVALID_REFRESH_TOKEN);
        }

        String requestHash = tokenHashEncoder.encode(refreshToken);

        if (!savedToken.matches(requestHash)) {
            throw new AppException(MemberErrorCode.INVALID_REFRESH_TOKEN);
        }

        if (savedToken.isRevoked()) {
            throw new AppException(MemberErrorCode.REVOKED_REFRESH_TOKEN);
        }

        if (savedToken.isExpired(LocalDateTime.now())) {
            throw new AppException(MemberErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        return savedToken;
    }

    //매시 정각마다 리프레시 토큰 중에서 만료시간이 된 토큰 청소
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanExpiredToken() {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}