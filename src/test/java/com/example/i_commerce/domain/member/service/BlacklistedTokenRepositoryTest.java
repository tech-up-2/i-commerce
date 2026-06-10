package com.example.i_commerce.domain.member.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.i_commerce.global.security.jwt.BlacklistedToken;
import com.example.i_commerce.global.security.jwt.BlacklistedTokenRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;

@DataJpaTest
public class BlacklistedTokenRepositoryTest {

    @Autowired
    private BlacklistedTokenRepository tokenRepository;

    @Test
    @DisplayName("만료 시간이 지난 블랙리스트 토큰만 선택적으로 삭제되어야 한다")
    void deleteExpiredTokensSuccess() {
        // given: 기준 시간 설정
        LocalDateTime now = LocalDateTime.now();

        // 1. 이미 만료된 토큰 (현재 시간보다 10분 전 만료)
        BlacklistedToken expiredToken1 = BlacklistedToken.builder()
            .tokenHash("expired-hash-1")
            .expiresAt(now.minusMinutes(10))
            .build();

        // 2. 이미 만료된 토큰 (현재 시간보다 1시간 전 만료)
        BlacklistedToken expiredToken2 = BlacklistedToken.builder()
            .tokenHash("expired-hash-2")
            .expiresAt(now.minusHours(1))
            .build();

        // 3. 아직 만료되지 않은 유효한 블랙리스트 토큰 (현재 시간보다 50분 뒤 만료)
        BlacklistedToken validToken = BlacklistedToken.builder()
            .tokenHash("valid-hash")
            .expiresAt(now.plusMinutes(50))
            .build();

        tokenRepository.saveAll(List.of(expiredToken1, expiredToken2, validToken));

        // when: '현재 시간 이전' 조건으로 삭제 요청 실행 (스케줄러가 호출할 JPQL 메서드)
        tokenRepository.deleteByExpiresAtBefore(now);

        // then: 결과 검증
        List<BlacklistedToken> remainingTokens = tokenRepository.findAll();

        // 1. 전체 테이블에 1개의 토큰만 남아있어야 함
        assertThat(remainingTokens).hasSize(1);

        // 2. 남아있는 토큰은 만료되지 않은 'valid-hash'여야 함
        assertThat(remainingTokens.get(0).getTokenHash()).isEqualTo("valid-hash");
    }

    @TestConfiguration
    static class TestCacheConfig {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }
}
