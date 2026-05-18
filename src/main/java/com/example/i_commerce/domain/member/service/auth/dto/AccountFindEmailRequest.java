package com.example.i_commerce.domain.member.service.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AccountFindEmailRequest(

    @NotBlank(message = "이름은 필수입니다.")
    String name,

    @NotBlank(message = "전화번호는 필수입니다.")
    String phoneNumber

) {

}
