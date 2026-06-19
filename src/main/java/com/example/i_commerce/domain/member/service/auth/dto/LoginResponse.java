package com.example.i_commerce.domain.member.service.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginResponse", description = "로그인 응답")
public record LoginResponse(
    Long memberId,
    String email,
    String accessToken,
    String refreshToken
) {

}
