package com.example.i_commerce.domain.product.facade.dto;

import com.example.i_commerce.domain.product.entity.ProductItemStatus;
import com.example.i_commerce.domain.product.repository.projection.ProductItemInfoProjection;
import lombok.Builder;

@Builder
public record ProductItemInfoResponse(
    Long productItemId,
    String productName,
    Long storeId,
    Integer price,
    String displayOptionName,
    Integer stockQuantity,
    boolean isOnSale
) {

    public static ProductItemInfoResponse from(ProductItemInfoProjection projection) {
        return ProductItemInfoResponse.builder()
            .productItemId(projection.getProductItemId())
            .productName(projection.getProductName())
            .storeId(projection.getStoreId())
            .price(projection.getPrice())
            .displayOptionName(projection.getDisplayOptionName())
            .stockQuantity(projection.getStockQuantity())
            .isOnSale(projection.getStatus() == ProductItemStatus.ON_SALE)
            .build();
    }

}
