package com.example.i_commerce.domain.order.service.dto;

import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;

public record OrderProductResponse(
    Long productId,
    Long userId,
    OrderStatus orderStatus
) {
}
