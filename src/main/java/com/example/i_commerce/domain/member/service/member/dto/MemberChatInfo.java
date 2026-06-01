package com.example.i_commerce.domain.member.service.member.dto;

import com.example.i_commerce.domain.member.entity.enums.MemberType;

public record MemberChatInfo(
    Long id,
    String name,
    MemberType role
) {

}
