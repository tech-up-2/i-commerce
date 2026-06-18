package com.example.i_commerce.domain.order.service.dto;

import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.Payment;
import lombok.Builder;

@Builder
public record CreateOrderResponse(
        Long orderId,
        Integer amount,
        String tossOrderId,
        String orderName
) {
    public static CreateOrderResponse of(Order order, String tossOrderId, String firstProductName) {
        return CreateOrderResponse.builder()
                .orderId(order.getId())
                .amount(order.getTotalPayAmount())
                .tossOrderId(tossOrderId)
                .orderName(firstProductName + "항목 외 " + (order.getOrderProducts().size() - 1) + "건")
                .build();
    }

}
