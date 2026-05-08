package com.example.i_commerce.domain.product.controller.response;

import com.example.i_commerce.domain.product.entity.ProductOptionValue;
import lombok.Builder;


@Builder
public record ProductOptionValueResponse(
    Long optionValueId,
    String value,
    Integer displayOrder,
    boolean selected,
    boolean available
) {

    public static ProductOptionValueResponse of(
        ProductOptionValue optionValue,
        boolean selected,
        boolean available
    ) {
        return ProductOptionValueResponse.builder()
            .optionValueId(optionValue.getId())
            .value(optionValue.getValue())
            .displayOrder(optionValue.getDisplayOrder())
            .selected(selected)
            .available(available)
            .build();
    }
}
