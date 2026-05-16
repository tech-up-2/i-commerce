package com.example.i_commerce.domain.order.event.dto;

public record PaymentApprovedEvent(
        Long orderId
) {
}
