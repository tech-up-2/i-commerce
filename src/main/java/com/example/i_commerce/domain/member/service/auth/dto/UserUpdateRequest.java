package com.example.i_commerce.domain.member.service.auth.dto;

import com.example.i_commerce.domain.member.entity.enums.Gender;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserUpdateRequest(

    @NotBlank(message = "이름은 필수입니다.")
    String name,

    @NotBlank(message = "전화번호는 필수입니다.")
    String phoneNumber,

    @NotNull(message = "성별은 필수입니다.")
    Gender gender,

    @NotBlank(message = "생년월일은 필수입니다.")
    String birthday

) {

}
