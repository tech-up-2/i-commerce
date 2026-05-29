package com.example.i_commerce.domain.product.application.dto;


import com.example.i_commerce.domain.cart.entity.CartItem;
import com.example.i_commerce.domain.product.entity.enums.ProductItemStatus;
import com.example.i_commerce.domain.product.repository.projection.ProductItemInfoProjection;
import lombok.Builder;

@Builder
public record ProductItemInfo(
    Long productItemId,
    String productName,
    Long storeId,
    Integer price,
    String displayOptionName,
    Integer stockQuantity,
    boolean isOnSale
) {

    public static ProductItemInfo from(ProductItemInfoProjection projection) {
        return ProductItemInfo.builder()
            .productItemId(projection.productItemId())
            .productName(projection.productName())
            .storeId(projection.storeId())
            .price(projection.price())
            .displayOptionName(projection.displayOptionName())
            .stockQuantity(projection.stockQuantity())
            .isOnSale(projection.productItemStatus() == ProductItemStatus.ON_SALE)
            .build();
    }

    public static ProductItemInfo unavailable(CartItem itemSnapshot) {
        return ProductItemInfo.builder()
            .productItemId(itemSnapshot.getProductItemId())
            .productName(itemSnapshot.getProductName())
            .storeId(itemSnapshot.getStoreId())
            .price(itemSnapshot.getSnapshotPrice())
            .displayOptionName(itemSnapshot.getDisplayOptionName())
            .isOnSale(false)
            .build();
    }
}
