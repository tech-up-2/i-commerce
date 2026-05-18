package com.example.i_commerce.domain.product.application.dto;

import com.example.i_commerce.domain.product.entity.ProductOptionValue;
import lombok.Builder;


@Builder
public record ProductOptionValueDto(
    Long optionValueId,
    String value,
    Integer displayOrder,
    boolean selected,
    boolean available
) {

    public static ProductOptionValueDto of(
        ProductOptionValue optionValue,
        boolean selected,
        boolean available
    ) {
        return ProductOptionValueDto.builder()
            .optionValueId(optionValue.getId())
            .value(optionValue.getValue())
            .displayOrder(optionValue.getDisplayOrder())
            .selected(selected)
            .available(available)
            .build();
    }
}
