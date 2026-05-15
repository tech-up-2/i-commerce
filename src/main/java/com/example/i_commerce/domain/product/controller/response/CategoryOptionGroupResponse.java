package com.example.i_commerce.domain.product.controller.response;

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
        String displayName,
        String inputType,
        Boolean required
    ){

    }

}
