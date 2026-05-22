package com.example.i_commerce.global.security.jwt;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlacklistedTokenService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final TokenHashUtil tokenHashUtil;
    private final JwtTokenUtil jwtTokenUtil;

    //로그아웃
    @Transactional
    public void logout(String token) {
        String tokenHash = tokenHashUtil.hash(token);
        LocalDateTime expiresAt = jwtTokenUtil.getExpiration(token);

        if (blacklistedTokenRepository.existsByTokenHashAndExpiresAtAfter(
            tokenHash,
            LocalDateTime.now()
        )) {
            return;
        }

        BlacklistedToken blacklistedToken = BlacklistedToken.builder()
            .tokenHash(tokenHash)
            .expiresAt(expiresAt)
            .build();

        blacklistedTokenRepository.save(blacklistedToken);
    }

    //토큰이 블랙리스트에 등록되어 있는지 확인하는 코드
    @Transactional(readOnly = true)
    public boolean isBlacklisted(String token) {
        String tokenHash = tokenHashUtil.hash(token);

        return blacklistedTokenRepository.existsByTokenHashAndExpiresAtAfter(
            tokenHash,
            LocalDateTime.now()
        );
    }

    //매시 정각마다 토큰 블랙리스트에서 만료시간이 된 토큰 청소
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanExpiredToken() {
        blacklistedTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }
}