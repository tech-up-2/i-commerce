package com.example.i_commerce.domain.member.service.dto;

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
