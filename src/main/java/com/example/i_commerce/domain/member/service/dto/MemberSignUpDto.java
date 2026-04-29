package com.example.i_commerce.domain.member.service.dto;

import com.example.i_commerce.domain.member.entity.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MemberSignUpDto(
    @NotBlank @Email
    String email,

    @NotBlank
    String password,

    @NotBlank
    String name,

    @NotNull
    Gender gender,

    @NotBlank
    String birthday,

    @NotBlank
    String phoneNumber
) {

}
