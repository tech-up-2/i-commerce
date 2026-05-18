package com.example.i_commerce.domain.product.controller.response;

import com.example.i_commerce.domain.product.application.dto.CategoryOptionGroupDto;
import java.util.List;
import lombok.Builder;

@Builder
public record CategoryOptionResponse(
    Long categoryId,
    List<CategoryOptionGroupDto> options
) {

    public static CategoryOptionResponse of(
        Long categoryId,
        List<CategoryOptionGroupDto> optionGroups
    ) {
        return CategoryOptionResponse.builder()
            .categoryId(categoryId)
            .options(optionGroups)
            .build();
    }
}
