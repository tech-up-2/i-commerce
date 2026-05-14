package com.example.i_commerce.domain.order.service.dto;

import com.example.i_commerce.domain.order.entity.Order;
import lombok.Builder;

@Builder
public record OrderSummaryResponse(
        Long orderId,
        Integer totalAmount,
        String status
) {
    public static OrderSummaryResponse of(Order order) {
        return OrderSummaryResponse.builder()
                .orderId(order.getId())
                .totalAmount(order.getTotalPayAmount())
                .status(order.getOrderStatus().name())
                .build();
    }
}
