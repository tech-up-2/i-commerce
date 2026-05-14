package com.example.i_commerce.domain.cart.controller.response;

import com.example.i_commerce.domain.cart.entity.CartItem;
import lombok.Builder;

@Builder
public record AddCartItemResponse(
    Long cartItemId,
    Long productItemId,
    String productName,
    Integer price,
    String displayOptionName,
    Integer quantity,
    Boolean isChecked
) {
    public static AddCartItemResponse from(CartItem cartItem) {
        return AddCartItemResponse.builder()
            .cartItemId(cartItem.getId())
            .productItemId(cartItem.getProductItemId())
            .productName(cartItem.getProductName())
            .price(cartItem.getPrice())
            .displayOptionName(cartItem.getDisplayOptionName())
            .quantity(cartItem.getQuantity())
            .isChecked(cartItem.getIsChecked())
            .build();
    }

}
