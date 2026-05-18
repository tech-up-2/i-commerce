package com.example.i_commerce.domain.member.service.admin.dto;

import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import com.example.i_commerce.domain.member.entity.enums.MemberType;
import java.time.LocalDateTime;

public record AdminMemberResponse(
    Long memberId,
    String email,
    String name,
    MemberType role,
    MemberStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}
