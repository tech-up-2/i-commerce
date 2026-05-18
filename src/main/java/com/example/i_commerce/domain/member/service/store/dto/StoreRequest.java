package com.example.i_commerce.domain.member.service.store.dto;

import jakarta.validation.constraints.NotBlank;

public record StoreRequest(
    @NotBlank
    String storeName,
    @NotBlank
    String phoneNumber
) {

}
