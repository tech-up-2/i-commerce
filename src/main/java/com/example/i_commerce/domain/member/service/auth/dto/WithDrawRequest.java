package com.example.i_commerce.domain.member.service.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record WithDrawRequest(
    @NotBlank String password
) {

}
