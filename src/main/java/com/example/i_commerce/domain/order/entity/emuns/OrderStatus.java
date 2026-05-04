package com.example.i_commerce.domain.order.entity.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatus {
    PENDING("주문 대기", "주문서가 생성되고 결제 대기 상태입니다."),
    CONFIRMED("주문 접수", "결제가 완료되어 배송을 준비 중입니다."),
    SHIPPING("배송 중", "상품이 배송 중입니다.(하나라도 출발 시)"),
    DELIVERED("배송 완료", "모든 상품이 배송지에 도착했습니다."),
    COMPLETED("구매 확정", ""),
    CANCELLED("주문 취소", "");

    private final String status;
    private final String description;

}
