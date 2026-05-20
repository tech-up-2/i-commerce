package com.example.i_commerce.global.security.jwt;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, Long> {

    boolean existsByTokenHashAndExpiresAtAfter(String tokenHash, LocalDateTime now);

    void deleteByExpiresAtBefore(LocalDateTime now);
}
