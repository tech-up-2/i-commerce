package com.example.i_commerce.domain.order.service.dto;

public record PaymentCancelPreparedDto(
        String tossOrderId,
        Long orderId
) {
}
