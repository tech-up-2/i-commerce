package com.example.i_commerce.domain.order.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "CreateOrderRequest", description = "주문 생성 요청")
public record CreateOrderRequest(
        Long memberId,
        Long addressId,
        // Long point
        List<OrderItemDto> items
) {

}
