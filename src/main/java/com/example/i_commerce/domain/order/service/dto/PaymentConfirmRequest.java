package com.example.i_commerce.domain.order.service.dto;

public record PaymentConfirmRequest(
        String paymentKey,
        String orderId,
        Integer amount
) {
}
