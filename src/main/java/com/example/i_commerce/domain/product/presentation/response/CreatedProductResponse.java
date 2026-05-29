package com.example.i_commerce.domain.product.presentation.response;

import com.example.i_commerce.domain.product.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Schema(name = "CreatedProductResponse", description = "상품 생성 응답")
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
