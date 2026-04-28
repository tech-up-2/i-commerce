package com.example.i_commerce.domain.order.service.dto;

public record OrderItemDto(
        Long productId,
        int quantity
) {
}
