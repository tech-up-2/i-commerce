package com.example.i_commerce.domain.member.service.admin.dto;

import com.example.i_commerce.domain.member.entity.enums.AdminRole;
import com.example.i_commerce.domain.member.entity.enums.AdminStatus;

public record AdminUpdateResponse(
    Long adminId,
    String email,
    String name,
    AdminRole adminRole,
    AdminStatus adminStatus
) {

}