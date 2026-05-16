package com.example.i_commerce.domain.product.controller.response;

import com.example.i_commerce.domain.product.application.dto.AlreadyExistsOption;
import java.util.List;
import lombok.Builder;

@Builder
public record AddCategoryOptionResponse(
    Long categoryId,
    List<AlreadyExistsOption> skippedOptions
) {

    public static AddCategoryOptionResponse of(
        Long categoryId,
        List<AlreadyExistsOption> alreadyExistsOptions
    ) {
        return AddCategoryOptionResponse.builder()
            .categoryId(categoryId)
            .skippedOptions(alreadyExistsOptions)
            .build();
    }

}
