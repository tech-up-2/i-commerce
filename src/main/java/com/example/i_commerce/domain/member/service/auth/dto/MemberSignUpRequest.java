package com.example.i_commerce.domain.member.service.auth.dto;

import com.example.i_commerce.domain.member.entity.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(name = "MemberSignUpRequest", description = "회원가입 요청")
public record MemberSignUpRequest(

    @Schema(description = "이메일", example = "test1@test.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    @Size(max = 254, message = "이메일은 254자를 초과할 수 없습니다.")
    String email,

    @Schema(description = "비밀번호", example = "password123!")
    @Size(min = 8, max = 64, message = "비밀번호는 8자 이상 64자 이하여야 합니다.")
    @NotBlank(message = "비밀번호는 필수입니다.")
    String password,

    @Schema(description = "이름", example = "홍길동")
    @Size(max = 20, message = "이름은 20자를 초과할 수 없습니다.")
    @NotBlank(message = "이름은 필수입니다.")
    String name,

    @Schema(description = "성별", example = "MALE")
    @NotNull(message = "성별은 필수입니다.")
    Gender gender,

    @Schema(description = "생년월일", example = "1998-05-20")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "생년월일은 yyyy-MM-dd 형식이어야 합니다.")
    @NotBlank(message = "생년월일은 필수입니다.")
    String birthday,

    @Schema(description = "휴대폰번호", example = "01012345678")
    @Size(max = 20, message = "전화번호는 20자를 초과할 수 없습니다.")
    @NotBlank(message = "전화번호는 필수입니다.")
    String phoneNumber
) {

}
