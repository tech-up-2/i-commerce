package com.example.i_commerce.domain.member.service.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "SignUpResponse", description = "회원가입 응답")
public record SignUpResponse(
    Long id,
    String email
) {

}
