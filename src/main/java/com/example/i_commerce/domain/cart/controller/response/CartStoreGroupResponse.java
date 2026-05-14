package com.example.i_commerce.domain.cart.controller.response;

import java.util.List;
import lombok.Builder;

@Builder
public record CartStoreGroupResponse(
    Long storeId,
    List<CartItemResponse> items,
    int storeTotalPrice
) {

    public static CartStoreGroupResponse of(
        Long storeId,
        List<CartItemResponse> items,
        int storeTotalPrice
    ) {
        return CartStoreGroupResponse.builder()
            .storeId(storeId)
            .items(items)
            .storeTotalPrice(storeTotalPrice)
            .build();
    }

}
