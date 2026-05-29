package com.example.i_commerce.domain.product.presentation.response;

import com.example.i_commerce.domain.product.entity.enums.OptionInputType;
import lombok.Builder;


@Builder
public record OptionResponse(
    Long id,
    String name,
    OptionInputType inputType

) {
    public static OptionResponse of(
        Long id, String name, OptionInputType inputType
    ) {
        return OptionResponse.builder()
            .id(id)
            .name(name)
            .inputType(inputType)
            .build();
    }

}
