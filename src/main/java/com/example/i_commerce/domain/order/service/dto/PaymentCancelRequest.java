package com.example.i_commerce.domain.order.service.dto;


public record PaymentCancelRequest(
        Long paymentId,
        Integer cancelAmount,
        String paymentKey,
        String cancelReason
) {
}
