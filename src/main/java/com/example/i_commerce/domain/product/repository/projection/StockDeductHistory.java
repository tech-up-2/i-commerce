package com.example.i_commerce.domain.product.repository.projection;

public record StockDeductHistory(
    Long productItemId,
    Integer changeQuantity
) {

}
