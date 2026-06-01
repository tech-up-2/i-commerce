package com.example.i_commerce.domain.order.service.dto;


public record PaymentCancelRequest(
        String tossOrderId,
        Integer cancelAmount,
        String paymentKey,
        String cancelReason
) {
}
