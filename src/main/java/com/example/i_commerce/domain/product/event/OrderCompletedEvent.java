package com.example.i_commerce.domain.product.event;


import java.util.List;

public record OrderCompletedEvent(
    Long orderId,
    List<OrderItemInfo> orderItems
) {
    public record OrderItemInfo(
        Long productItemId,
        int quantity
    ) {}
}
