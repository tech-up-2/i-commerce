package com.example.i_commerce.domain.product.presentation.response;

import com.example.i_commerce.domain.product.entity.ProductAttribute;
import com.example.i_commerce.domain.product.entity.ProductItem;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;

@Builder
public record ProductItemResponse(
    Long itemId,
    String sku,
    Integer price,
    String mainImageUrl,
    String status,
    String displayOptionName,
    List<ProductAttributeResponse> attributes
) {

    public static ProductItemResponse of(
        ProductItem item,
        List<ProductAttribute> attributes
    ) {
        return ProductItemResponse.builder()
            .itemId(item.getId())
            .sku(item.getSku())
            .price(item.getPrice())
            .mainImageUrl(item.getMainImageUrl())
            .status(item.getStatus().name())
            .displayOptionName(item.getDisplayOptionName())
            .attributes(attributes.stream()
                .map(ProductAttributeResponse::of)
                .collect(Collectors.toList())
            )
            .build();
    }
}
