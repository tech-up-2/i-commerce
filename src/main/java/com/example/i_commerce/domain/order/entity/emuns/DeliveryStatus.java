package com.example.i_commerce.domain.order.entity.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DeliveryStatus {
    PREPARING("상품 준비중", "결제가 완료되어 상품을 준비 중입니다."),
    SHIPPING("배송 중", "상품이 배송 중입니다."),
    ARRIVED("배송 완료", "상품이 배송지에 도착했습니다.");

    private final String status;
    private final String description;
}
