package com.example.i_commerce.domain.member.controller;

import com.example.i_commerce.domain.member.service.auth.dto.TokenTestResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth/test")
public class AuthTestController {

    @GetMapping("/token")
    public ResponseEntity<TokenTestResponse> testToken(
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return ResponseEntity.ok(
            new TokenTestResponse(
                principal.getId(),
                principal.getType().name(),
                "토큰 인증 성공"
            )
        );
    }
}