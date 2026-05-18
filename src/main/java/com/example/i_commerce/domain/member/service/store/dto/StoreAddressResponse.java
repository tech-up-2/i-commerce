package com.example.i_commerce.domain.member.service.store.dto;

import com.example.i_commerce.domain.member.entity.StoreAddress;
import com.example.i_commerce.domain.member.entity.enums.AddressType;

public record StoreAddressResponse(
    Long storeAddressId,
    AddressType addressType,
    String label,
    String addressPhoneNumber,
    String zipCode,
    String roadAddress,
    String jibunAddress,
    String detailAddress,
    String extraAddress,
    Boolean isDefault
) {
    public static StoreAddressResponse from(StoreAddress storeAddress){
        return new StoreAddressResponse(
            storeAddress.getId(),
            storeAddress.getAddressType(),
            storeAddress.getLabel(),
            storeAddress.getAddressPhoneNumber(),
            storeAddress.getZipCode(),
            storeAddress.getRoadAddress(),
            storeAddress.getJibunAddress(),
            storeAddress.getDetailAddress(),
            storeAddress.getExtraAddress(),
            storeAddress.getIsDefault()
        );
    }
}
