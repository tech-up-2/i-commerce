package com.example.i_commerce.domain.member.service.auth.dto;

public record TokenTestResponse(
    Long accountId,
    String principalType,
    String message
) {

}
