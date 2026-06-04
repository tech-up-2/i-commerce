package com.example.i_commerce.domain.product.application.dto;

import com.example.i_commerce.domain.product.entity.enums.OptionInputType;
import com.example.i_commerce.domain.product.repository.projection.CategoryOptionProjection;
import lombok.Builder;

@Builder
public record CategoryOptionDto(
    Long categoryOptionId,
    Long optionId,
    String name,
    OptionInputType inputType,
    Boolean required
) {

    public static CategoryOptionDto from(CategoryOptionProjection projection) {
        return CategoryOptionDto.builder()
            .categoryOptionId(projection.categoryOptionId())
            .optionId(projection.optionId())
            .name(projection.optionName())
            .inputType(projection.optionInputType())
            .required(projection.required())
            .build();
    }

}
