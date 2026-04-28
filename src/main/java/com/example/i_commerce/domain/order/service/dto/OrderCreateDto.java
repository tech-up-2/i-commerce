package com.example.i_commerce.domain.order.service.dto;

import java.util.List;

public record OrderCreateDto(
        Long memberId,
        // Long point
        List<OrderItemDto> items
) {
}
