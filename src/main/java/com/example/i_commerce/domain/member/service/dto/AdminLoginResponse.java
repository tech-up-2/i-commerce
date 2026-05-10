package com.example.i_commerce.domain.member.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AdminLoginResponse", description = "관리자 로그인 응답")
public record AdminLoginResponse(
    Long memberId,
    String accessToken
) {

}
