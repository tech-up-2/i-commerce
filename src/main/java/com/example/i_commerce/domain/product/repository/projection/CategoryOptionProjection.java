package com.example.i_commerce.domain.product.repository.projection;

import com.example.i_commerce.domain.product.entity.enums.OptionInputType;

public record CategoryOptionProjection(
    Long categoryOptionId,
    Boolean required,
    Long optionId,
    String optionName,
    OptionInputType optionInputType
) {

}
