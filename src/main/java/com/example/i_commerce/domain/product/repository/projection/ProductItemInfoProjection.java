package com.example.i_commerce.domain.product.repository.projection;

import com.example.i_commerce.domain.product.entity.enums.ProductItemStatus;

public record ProductItemInfoProjection(
    Long productItemId,
    String productName,
    Long storeId,
    Integer price,
    String displayOptionName,
    Integer stockQuantity,
    ProductItemStatus productItemStatus
) {

}
