package com.example.i_commerce.domain.product.controller.response;

import com.example.i_commerce.domain.product.entity.Category;
import lombok.Builder;

@Builder
public record CreateCategoryResponse(
    Long id
) {
    public static CreateCategoryResponse from(Category category) {
        return CreateCategoryResponse.builder()
            .id(category.getId())
            .build();
    }
}
