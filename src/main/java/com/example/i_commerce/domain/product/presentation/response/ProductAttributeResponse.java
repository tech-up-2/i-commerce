package com.example.i_commerce.domain.product.presentation.response;

import com.example.i_commerce.domain.product.entity.ProductAttribute;
import lombok.Builder;

@Builder
public record ProductAttributeResponse(
    Long attributeId,
    String displayName,
    Integer displayOrder
) {

    public static ProductAttributeResponse of(ProductAttribute attr) {
        return ProductAttributeResponse.builder()
            .attributeId(attr.getId())
            .displayName(attr.getDisplayName())
            .displayOrder(attr.getDisplayOrder())
            .build();
    }

}
