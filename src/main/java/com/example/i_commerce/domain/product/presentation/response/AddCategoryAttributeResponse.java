package com.example.i_commerce.domain.product.presentation.response;

import java.util.List;
import lombok.Builder;

@Builder
public record AddCategoryAttributeResponse(
    Long categoryId,
    List<AlreadyExistsAttribute> skippedAttributes
) {
    public static AddCategoryAttributeResponse of(
        Long categoryId,
        List<AlreadyExistsAttribute> alreadyExistsAttributes
    ) {
        return AddCategoryAttributeResponse.builder()
            .categoryId(categoryId)
            .skippedAttributes(alreadyExistsAttributes)
            .build();
    }

    @Builder
    public record AlreadyExistsAttribute(
        Long categoryId,
        Long attributeId
    ) {
        public static AlreadyExistsAttribute of(Long categoryId, Long attributeId) {
            return AlreadyExistsAttribute.builder()
                .categoryId(categoryId)
                .attributeId(attributeId)
                .build();
        }
    }
}
