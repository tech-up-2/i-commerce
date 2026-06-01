package com.example.i_commerce.domain.product.presentation.response;

import com.example.i_commerce.domain.product.entity.enums.ProductItemStatus;

public record ProductItemSearchResponse(
    Long productItemId,
    Long productId,
    String productName,
    String displayOptionName,
    Integer price,
    String mainImageUrl,
    ProductItemStatus itemStatus,
    String categoryName,
    Double relevanceScore
) {

}
