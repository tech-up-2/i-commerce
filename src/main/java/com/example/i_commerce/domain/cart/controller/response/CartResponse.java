package com.example.i_commerce.domain.cart.controller.response;

import com.example.i_commerce.domain.cart.entity.Cart;
import java.util.List;
import lombok.Builder;

@Builder
public record CartResponse(
    Long cartId,
    List<CartStoreGroupResponse> storeGroups,
    int totalCheckedPrice
) {

    public static CartResponse of(
        Cart cart,
        List<CartStoreGroupResponse> storeGroups,
        int totalCheckedPrice
    ) {
        return CartResponse.builder()
            .cartId(cart.getId())
            .storeGroups(storeGroups)
            .totalCheckedPrice(totalCheckedPrice)
            .build();
    }

    public static CartResponse empty(Long cartId) {
        return CartResponse.builder()
            .cartId(cartId)
            .storeGroups(List.of())
            .totalCheckedPrice(0)
            .build();
    }
}
