package com.example.i_commerce.domain.order.event.dto;

public record OrderCreatedEvent(
//        주문ID, 결제ID, 회원ID, 금액
        Long orderId,
        Long paymentId,
        Long userId,
        int paymentPrice
) {
}
