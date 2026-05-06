package com.example.i_commerce.domain.member.service.dto;

public record DeliveryAddressSnapshot(//주문쪽으로 보낼 정보들
                                      String recipientName,
                                      String recipientPhone,
                                      String zipCode,
                                      String roadAddress,
                                      String jibunAddress,
                                      String detailAddress,
                                      String extraAddress,
                                      String deliveryMemo
) {

}
