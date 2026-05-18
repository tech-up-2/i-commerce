package com.example.i_commerce.domain.member.service.admin.dto;

import com.example.i_commerce.domain.member.entity.enums.MemberStatus;
import jakarta.validation.constraints.NotNull;

public record AdminMemberStatusUpdateRequest(
    @NotNull
    MemberStatus memberStatus
) {

}