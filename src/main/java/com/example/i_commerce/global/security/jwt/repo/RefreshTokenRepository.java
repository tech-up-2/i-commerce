package com.example.i_commerce.global.security.jwt.repo;

import com.example.i_commerce.global.security.jwt.entity.RefreshToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenId(String s);
}
