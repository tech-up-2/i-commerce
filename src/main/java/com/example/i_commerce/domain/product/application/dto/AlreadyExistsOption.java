package com.example.i_commerce.domain.product.application.dto;

import lombok.Builder;

@Builder
public record AlreadyExistsOption(
    Long categoryId,
    Long optionId
) {

    public static AlreadyExistsOption of(Long categoryId, Long optionId) {
        return AlreadyExistsOption.builder()
            .categoryId(categoryId)
            .optionId(optionId)
            .build();
    }

}
