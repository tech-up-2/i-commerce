package com.example.i_commerce.domain.testtools;

import com.example.i_commerce.domain.member.entity.DeliveryAddress;
import com.example.i_commerce.domain.member.tools.DataEncryptor;

public class DeliveryAddressFixture {

    //기본
    public static DeliveryAddress createDeliveryAddress(
        Long memberId,
        boolean isDefault,
        DataEncryptor dataEncryptor
    ) {
        return DeliveryAddress.builder()
            .memberId(memberId)
            .label(memberId + "의 집")
            .recipientName(dataEncryptor.encrypt("홍길동"))
            .recipientPhone(dataEncryptor.encrypt("010" + createRandomPhoneTail()))
            .zipCode(dataEncryptor.encrypt("06236"))
            .roadAddress(dataEncryptor.encrypt("서울특별시 강남구 테헤란로 123"))
            .jibunAddress(dataEncryptor.encrypt("서울특별시 강남구 역삼동 123"))
            .detailAddress(dataEncryptor.encrypt("101동 1001호"))
            .extraAddress(dataEncryptor.encrypt("테스트주소"))
            .isDefault(isDefault)
            .build();
    }

    //사용자 지정 라벨
    public static DeliveryAddress createDeliveryAddress(
        String label,
        Long memberId,
        boolean isDefault,
        DataEncryptor dataEncryptor
    ) {
        return DeliveryAddress.builder()
            .memberId(memberId)
            .label(label)
            .recipientName(dataEncryptor.encrypt("홍길동"))
            .recipientPhone(dataEncryptor.encrypt("010" + createRandomPhoneTail()))
            .zipCode(dataEncryptor.encrypt("06236"))
            .roadAddress(dataEncryptor.encrypt("서울특별시 강남구 테헤란로 123"))
            .jibunAddress(dataEncryptor.encrypt("서울특별시 강남구 역삼동 123"))
            .detailAddress(dataEncryptor.encrypt("101동 1001호"))
            .extraAddress(dataEncryptor.encrypt("테스트주소"))
            .isDefault(isDefault)
            .build();
    }

    private static String createRandomPhoneTail() {
        return String.valueOf(System.nanoTime()).substring(5, 13);
    }
}
