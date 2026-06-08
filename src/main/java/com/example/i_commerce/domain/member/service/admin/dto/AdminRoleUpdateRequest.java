package com.example.i_commerce.domain.member.service.admin.dto;

import com.example.i_commerce.domain.member.entity.enums.AdminRole;
import jakarta.validation.constraints.NotNull;

public record AdminRoleUpdateRequest(
    @NotNull(message = "관리자 권한은 필수입니다.")
    AdminRole adminRole
) {

}