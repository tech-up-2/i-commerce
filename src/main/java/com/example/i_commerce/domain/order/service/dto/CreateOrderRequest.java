package com.example.i_commerce.domain.order.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "CreateOrderRequest", description = "주문 생성 요청")
public record CreateOrderRequest(

        @Schema(description = "배송지 주소 ID", example = "1")
        Long addressId,
        // Long point

        @Schema(example = "[{\"productId\": 1, \"quantity\": 1}, {\"productId\": 2, \"quantity\": 3}]")
        List<OrderItemDto> items
) {

        public record OrderItemDto(
                @Schema(description = "상품 ID", example = "1")
                Long productId,

                @Schema(description = "주문 수량", example = "1")
                Integer quantity
        ) {
        }
}
