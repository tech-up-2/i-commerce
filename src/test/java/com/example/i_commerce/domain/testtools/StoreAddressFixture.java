package com.example.i_commerce.domain.testtools;

import com.example.i_commerce.domain.member.entity.StoreAddress;
import com.example.i_commerce.domain.member.entity.enums.AddressType;

public class StoreAddressFixture {

    //기본
    public static StoreAddress createStoreAddress(
        Long storeId,
        boolean isDefault
    ) {
        return StoreAddress.builder()
            .storeId(storeId)
            .addressType(AddressType.BUSINESS)
            .label(storeId + "의 사업장")
            .addressPhoneNumber("02" + createRandomNumber(8))
            .zipCode("06236")
            .roadAddress("서울특별시 강남구 테헤란로 123")
            .jibunAddress("서울특별시 강남구 역삼동 123")
            .detailAddress("5층")
            .extraAddress("테스트상점주소")
            .isDefault(isDefault)
            .build();
    }

    //사용자 설정 라벨
    public static StoreAddress createStoreAddress(
        String label,
        Long storeId,
        boolean isDefault
    ) {
        return StoreAddress.builder()
            .storeId(storeId)
            .addressType(AddressType.BUSINESS)
            .label(label)
            .addressPhoneNumber("02" + createRandomNumber(8))
            .zipCode("06236")
            .roadAddress("서울특별시 강남구 테헤란로 123")
            .jibunAddress("서울특별시 강남구 역삼동 123")
            .detailAddress("5층")
            .extraAddress("테스트상점주소")
            .isDefault(isDefault)
            .build();
    }

    private static String createRandomNumber(int length) {
        String value = String.valueOf(System.nanoTime());
        return value.substring(value.length() - length);
    }
}