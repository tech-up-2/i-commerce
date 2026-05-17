package com.example.i_commerce.domain.member.service.store.dto;

import com.example.i_commerce.domain.member.entity.enums.AddressType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(name = "StoreAddressRequest", description = "상점 주소 등록 요청")
public record StoreAddressRequest(
    @NotBlank AddressType addressType,
    @NotBlank String label,
    @NotBlank String addressPhoneNumber,
    @NotBlank String zipCode,
    @NotBlank String roadAddress,
    @NotBlank String jibunAddress,
    @NotBlank String detailAddress,
    @NotBlank String extraAddress,
    @NotBlank Boolean isDefault
) {

}
