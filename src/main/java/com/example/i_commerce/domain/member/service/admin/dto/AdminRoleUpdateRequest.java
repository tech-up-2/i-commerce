package com.example.i_commerce.domain.member.service.admin.dto;

import com.example.i_commerce.domain.member.entity.enums.AdminRole;
import jakarta.validation.constraints.NotNull;

public record AdminRoleUpdateRequest(
    @NotNull
    AdminRole adminRole
) {

}