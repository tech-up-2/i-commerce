package com.example.i_commerce.domain.member.service.dto;

public record AdminLoginResponse(
    Long memberId,
    String accessToken
) {

}
