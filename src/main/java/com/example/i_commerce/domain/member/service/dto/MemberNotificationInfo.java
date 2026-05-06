package com.example.i_commerce.domain.member.service.dto;

public record MemberNotificationInfo(
    Long memberId,
    String email,
    String phoneNumber
) {

}
