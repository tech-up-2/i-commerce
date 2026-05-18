package com.example.i_commerce.domain.member.service.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record PasswordResetRequest(
    @NotBlank String oldPassword,
    @NotBlank String newPassword
) {

}
