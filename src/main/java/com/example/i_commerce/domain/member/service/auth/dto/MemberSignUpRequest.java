package com.example.i_commerce.domain.member.service.auth.dto;

import com.example.i_commerce.domain.member.entity.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(name = "MemberSignUpRequest", description = "회원가입 요청")
public record MemberSignUpRequest(
    @Schema(description = "이메일", example = "test1@test.com")
    @NotBlank @Email
    String email,

    @Schema(description = "비밀번호", example = "password123!")
    @NotBlank
    String password,

    @Schema(description = "이름", example = "홍길동")
    @NotBlank
    String name,

    @Schema(description = "성별", example = "MALE")
    @NotNull
    Gender gender,

    @Schema(description = "생년월일", example = "1998-05-20")
    @NotBlank
    String birthday,

    @Schema(description = "휴대폰번호", example = "01012345678")
    @NotBlank
    String phoneNumber
) {

}
