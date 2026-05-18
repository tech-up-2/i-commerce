package com.example.i_commerce.domain.member.service.store.dto;

import com.example.i_commerce.domain.member.entity.enums.StoreStatus;
import jakarta.validation.constraints.NotBlank;

public record StoreUpdateRequest(
    @NotBlank
    String storeName,
    @NotBlank
    String phoneNumber,
    @NotBlank
    StoreStatus storeStatus
) {

}
