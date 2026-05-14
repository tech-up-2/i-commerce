package com.example.i_commerce.domain.cart.controller.response;

import com.example.i_commerce.domain.cart.entity.CartItem;
import com.example.i_commerce.domain.cart.infrastructure.ProductItemInfo;
import lombok.Builder;

@Builder
public record CartItemResponse(
    Long cartItemId,
    Long productItemId,
    String productName,
    int snapshotPrice,
    int currentPrice,
    int quantity,
    int itemTotalPrice,
    boolean isChecked,
    boolean isOnSale
) {

    public static CartItemResponse of(
        CartItem cartItem,
        ProductItemInfo productInfo
    ) {

        return CartItemResponse.builder()
            .cartItemId(cartItem.getId())
            .productItemId(cartItem.getProductItemId())
            .productName(productInfo.productName())
            .snapshotPrice(cartItem.getSnapshotPrice())
            .currentPrice(productInfo.price())
            .quantity(cartItem.getQuantity())
            .itemTotalPrice(productInfo.price() * cartItem.getQuantity())
            .isChecked(cartItem.getIsChecked())
            .isOnSale(productInfo.isOnSale())
            .build();
    }

}
