package com.example.i_commerce.domain.cart.infrastructure;


import com.example.i_commerce.domain.cart.entity.CartItem;
import com.example.i_commerce.domain.product.facade.dto.ProductItemInfoResponse;
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

    public static ProductItemInfo from(ProductItemInfoResponse response) {
        return ProductItemInfo.builder()
            .productItemId(response.productItemId())
            .productName(response.productName())
            .storeId(response.storeId())
            .price(response.price())
            .displayOptionName(response.displayOptionName())
            .stockQuantity(response.stockQuantity())
            .isOnSale(response.isOnSale())
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
