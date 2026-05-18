package com.example.i_commerce.domain.product.application.dto;

import com.example.i_commerce.domain.product.entity.OptionInputType;
import java.util.List;
import lombok.Builder;

@Builder
public record CategoryOptionGroupDto(
    String optionType,
    List<CategoryOptionValueDto> optionValues
) {

    @Builder
    public record CategoryOptionValueDto(
        Long categoryOptionId,
        Long optionId,
        String value,
        OptionInputType inputType,
        Boolean required
    ){

    }

}
