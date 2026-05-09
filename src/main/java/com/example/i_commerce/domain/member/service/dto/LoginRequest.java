package com.example.i_commerce.domain.member.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "LoginRequest", description = "로그인 요청")
public record LoginRequest(
    @Schema(description = "이메일", example = "test1@test.com")
    @NotBlank @Email
    String email,

    @Schema(description = "비밀번호", example = "password123!")
    @NotBlank
    String password
) {

}
