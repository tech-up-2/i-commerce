package com.example.i_commerce.domain.order.service.dto;

import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.Payment;
import lombok.Builder;

@Builder
public record CreateOrderResponse(
//        Long orderId,
        Long paymentId,
        Integer amount,
        String orderName
) {
    public static CreateOrderResponse of(Order order, Payment payment, String firstProductName) {
        return CreateOrderResponse.builder()
//                .orderId(order.getId())
                .paymentId(payment.getId())
                .amount(order.getTotalPayAmount())
                .orderName(firstProductName + "항목 외 " + (order.getOrderProducts().size() - 1) + "건")
                .build();
    }

}
