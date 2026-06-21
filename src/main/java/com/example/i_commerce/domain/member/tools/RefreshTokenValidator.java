package com.example.i_commerce.domain.member.tools;

import com.example.i_commerce.domain.member.exception.MemberErrorCode;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.security.jwt.JwtTokenUtil;
import com.example.i_commerce.global.security.jwt.TokenHashEncoder;
import com.example.i_commerce.global.security.jwt.dto.RefreshTokenPayload;
import com.example.i_commerce.global.security.jwt.entity.RefreshToken;
import com.example.i_commerce.global.security.jwt.repo.RefreshTokenRepository;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
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
        RefreshTokenPayload payload = jwtTokenUtil.parseRefreshToken(refreshToken);

        if (payload.principalType() != expectedPrincipalType) {
            throw new AppException(MemberErrorCode.INVALID_REFRESH_TOKEN);
        }

        RefreshToken savedToken = refreshTokenRepository.findByTokenId(payload.tokenId())
            .orElseThrow(() -> new AppException(MemberErrorCode.INVALID_REFRESH_TOKEN));

        String requestHash = tokenHashEncoder.encode(refreshToken);

        if (!savedToken.matches(requestHash)) {
            throw new AppException(MemberErrorCode.INVALID_REFRESH_TOKEN);
        }

        LocalDateTime now = LocalDateTime.now();

        if (savedToken.isRevoked()) {
            throw new AppException(MemberErrorCode.REVOKED_REFRESH_TOKEN);
        }

        if (savedToken.isExpired(now)) {
            throw new AppException(MemberErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        return payload;
    }
}