package com.example.i_commerce.domain.member.service.dto;

public record LoginResponse(
    Long memberId,
    String email,
    String accessToken
) {

}
