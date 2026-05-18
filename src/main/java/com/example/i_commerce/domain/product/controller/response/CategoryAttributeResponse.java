package com.example.i_commerce.domain.product.controller.response;

import com.example.i_commerce.domain.product.application.dto.CategoryAttributeGroupDto;
import java.util.List;
import lombok.Builder;

@Builder
public record CategoryAttributeResponse(
    Long categoryId,
    List<CategoryAttributeGroupDto> attributes
) {
    public static CategoryAttributeResponse of(
        Long categoryId,
        List<CategoryAttributeGroupDto> attributes
    ) {
        return CategoryAttributeResponse.builder()
            .categoryId(categoryId)
            .attributes(attributes)
            .build();
    }
}
