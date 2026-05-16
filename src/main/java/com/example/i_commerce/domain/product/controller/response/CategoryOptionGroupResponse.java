package com.example.i_commerce.domain.product.controller.response;

import com.example.i_commerce.domain.product.entity.OptionInputType;
import java.util.List;
import lombok.Builder;

@Builder
public record CategoryOptionGroupResponse(
    String optionType,
    List<CategoryOptionValueResponse> optionValues
) {

    @Builder
    public record CategoryOptionValueResponse(
        Long categoryOptionId,
        Long optionId,
        String value,
        OptionInputType inputType,
        Boolean required
    ){

    }

}
