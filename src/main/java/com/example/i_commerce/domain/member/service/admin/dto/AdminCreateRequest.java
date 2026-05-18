package com.example.i_commerce.domain.member.service.admin.dto;

import com.example.i_commerce.domain.member.entity.enums.AdminRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "AdminLignUpRequest", description = "관리자 회원가입 요청")
public record AdminCreateRequest(
    @Schema(description = "이메일", example = "admin1@test.com")
    @NotBlank @Email
    String email,

    @Schema(description = "비밀번호", example = "password123!")
    @NotBlank
    String password,

    @Schema(description = "이름", example = "홍길동")
    @NotBlank
    String name,

    @Schema(description = "관리자 역할", example = "ADMIN")
    @NotNull
    AdminRole adminRole
) {

}
