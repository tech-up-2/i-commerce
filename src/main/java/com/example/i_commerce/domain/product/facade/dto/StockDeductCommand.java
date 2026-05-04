package com.example.i_commerce.domain.product.facade.dto;

public record StockDeductCommand(
    Long productItemId,
    int quantity,
    Long orderId
) {

}
