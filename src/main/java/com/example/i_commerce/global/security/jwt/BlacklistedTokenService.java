package com.example.i_commerce.global.security.jwt;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BlacklistedTokenService {

    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final TokenHashUtil tokenHashUtil;

    @Transactional(readOnly = true)
    public boolean isBlacklisted(String token) {
        String tokenHash = tokenHashUtil.hash(token);

        return blacklistedTokenRepository.existsByTokenHashAndExpiresAtAfter(
            tokenHash,
            LocalDateTime.now()
        );
    }
}