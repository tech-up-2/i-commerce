package com.example.i_commerce.domain.member.service.dto;

//주문쪽으로 보낼 정보들
public record DeliveryAddressSnapshot(
    //수령자 이름
    String recipientName,

    //수령자 전화번호
    String recipientPhone,

    //우편번호
    String zipCode,

    //도로면 주소
    String roadAddress,

    //지번주소
    String jibunAddress,

    //상세주소
    String detailAddress,

    //참고항목
    String extraAddress,

    //배송 메모
    String deliveryMemo
) {

}
