package com.example.i_commerce.domain.member.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "DeliveryAddressResponse", description = "배송지 조회 응답")
public record DeliveryAddressResponse(
    Long id,
    String label,
    String recipientName,
    String recipientPhone,
    String zipCode,
    String roadAddress,
    String jibunAddress,
    String detailAddress,
    String extraAddress,
    Boolean isDefault,
    String deliveryMemo
) {

}
