package com.example.i_commerce.domain.product.presentation.response;

import com.example.i_commerce.domain.product.application.dto.CategoryOptionDto;
import java.util.List;
import lombok.Builder;

@Builder
public record CategoryOptionResponse(
    Long categoryId,
    List<CategoryOptionDto> options
) {

    public static CategoryOptionResponse of(
        Long categoryId,
        List<CategoryOptionDto> options
    ) {
        return CategoryOptionResponse.builder()
            .categoryId(categoryId)
            .options(options)
            .build();
    }
}
