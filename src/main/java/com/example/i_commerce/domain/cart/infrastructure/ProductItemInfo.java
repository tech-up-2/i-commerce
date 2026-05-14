package com.example.i_commerce.domain.cart.infrastructure;


import lombok.Builder;

@Builder
public record ProductItemInfo(
    Long productItemId,
    String productName,
    Long storeId,
    Integer price,
    String displayOptionName,
    Integer stockQuantity,
    boolean isAvailable
) {

}
