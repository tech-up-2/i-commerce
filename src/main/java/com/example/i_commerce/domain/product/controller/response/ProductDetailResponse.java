package com.example.i_commerce.domain.product.controller.response;

import com.example.i_commerce.domain.product.application.dto.OptionItemLookupDto;
import com.example.i_commerce.domain.product.application.dto.ProductOptionGroupDto;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductAttribute;
import com.example.i_commerce.domain.product.entity.ProductImage;
import com.example.i_commerce.domain.product.entity.ProductItem;
import java.util.List;
import lombok.Builder;

@Builder
public record ProductDetailResponse(
    Long productId,
    String name,
    String description,
    String status,
    String optionType,
    Long storeId,
    String categoryName,
    List<String> imageUrls,
    ProductItemResponse selectedItem,
    List<ProductOptionGroupDto> optionGroups,
    OptionItemLookupDto optionItemLookup
) {


    public static ProductDetailResponse of(
        Product product,
        List<ProductImage> images,
        ProductItem selectedItem,
        List<ProductAttribute> attributes,
        List<ProductOptionGroupDto> optionGroups,
        OptionItemLookupDto optionItemLookup
    ) {
        return ProductDetailResponse.builder()
            .productId(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .status(product.getStatus().name())
            .optionType(product.getOptionType().name())
            .storeId(product.getStoreId())
            .categoryName(product.getCategory().getName())
            .imageUrls(images.stream().map(ProductImage::getImageUrl).toList())
            .selectedItem(ProductItemResponse.of(selectedItem, attributes))
            .optionGroups(optionGroups)
            .optionItemLookup(optionItemLookup)
            .build();
    }


}
