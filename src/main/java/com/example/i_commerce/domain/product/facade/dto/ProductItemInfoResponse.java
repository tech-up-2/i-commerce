package com.example.i_commerce.domain.product.facade.dto;

import lombok.Builder;

@Builder
public record ProductItemInfoResponse(
    Long productItemId,
    String productName,
    Long storeId,
    Integer price,
    String displayOptionName,
    Integer stockQuantity,
    boolean onSale
) {

}
