package com.example.i_commerce.domain.order.event.dto;

public record PaymentCompletedEvent(
        Long orderId,
        Long paymentId,
        Long userId
) {
}
