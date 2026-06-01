package com.example.i_commerce.domain.order.entity.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeliveryStatus {
    PREPARING("상품 준비중", "결제가 완료되어 상품을 준비 중입니다."),
    DELIVERY_HOLD("출고 대기", "결제 취소 요청이 들어왔으나, 환불 여부가 불확실하여 출고를 일시 정지시킨 상태입니다."),
    SHIPPING("배송 중", "상품이 배송 중입니다."),
    ARRIVED("배송 완료", "상품이 배송지에 도착했습니다."),
    CANCEL_REQUESTED("배송 취소 요청", "상품 준비중 결제 취소 요청이 들어왔습니다."),
    CANCELLED("배송 취소", "상품 준비중 결제 취소로 인해 배송이 취소되었습니다.");

    private final String status;
    private final String description;
}
