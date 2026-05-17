package com.example.i_commerce.domain.member.service.admin.dto;

import com.example.i_commerce.domain.member.entity.enums.AdminRole;
import com.example.i_commerce.domain.member.entity.enums.AdminStatus;

public record AdminCreateResponse(
    Long adminId,
    String email,
    String name,
    AdminRole role,
    AdminStatus status
) {

}
