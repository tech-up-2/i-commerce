package com.example.i_commerce.domain.product.presentation.response;

import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.enums.ProductStatus;
import lombok.Builder;

@Builder
public record UpdateProductStatusResponse(
    Long productId,
    ProductStatus status
) {
    public static UpdateProductStatusResponse from(Product product) {
        return UpdateProductStatusResponse.builder()
            .productId(product.getId())
            .status(product.getStatus())
            .build();
    }
}
