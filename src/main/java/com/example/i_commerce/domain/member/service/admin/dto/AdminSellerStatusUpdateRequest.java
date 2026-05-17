package com.example.i_commerce.domain.member.service.admin.dto;

import com.example.i_commerce.domain.member.entity.enums.SellerStatus;
import jakarta.validation.constraints.NotNull;

public record AdminSellerStatusUpdateRequest(
    @NotNull
    SellerStatus sellerStatus
) {

}