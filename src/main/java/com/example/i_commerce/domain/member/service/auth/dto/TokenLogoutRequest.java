package com.example.i_commerce.domain.member.service.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record TokenLogoutRequest(
    @NotBlank(message = "Refresh Token은 필수입니다.")
    String refreshToken
) {

}
