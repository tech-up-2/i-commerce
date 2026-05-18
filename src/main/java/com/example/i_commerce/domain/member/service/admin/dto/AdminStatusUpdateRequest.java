package com.example.i_commerce.domain.member.service.admin.dto;

import com.example.i_commerce.domain.member.entity.enums.AdminStatus;
import jakarta.validation.constraints.NotNull;

public record AdminStatusUpdateRequest(
    @NotNull
    AdminStatus adminStatus
) {

}