package com.example.i_commerce.domain.member.service.admin.dto;


import com.example.i_commerce.domain.member.entity.enums.AdminRole;
import com.example.i_commerce.domain.member.entity.enums.AdminStatus;
import java.time.LocalDateTime;

public record AdminInfoResponse(
    Long adminId,
    String email,
    String name,
    AdminRole adminRole,
    AdminStatus adminStatus,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {

}
