package com.example.i_commerce.domain.product.controller.response;

import com.example.i_commerce.domain.product.entity.Product;
import lombok.Builder;

@Builder
public record CreatedProductResponse(
    Long productId
) {
    public static CreatedProductResponse from(Product product) {
        return CreatedProductResponse.builder()
            .productId(product.getId())
            .build();
    }
}
