package com.example.i_commerce.domain.product.application.dto;

import lombok.Builder;

@Builder
public record StockDeductCommand(
    Long productItemId,
    int quantity,
    Long orderId
) {

}
