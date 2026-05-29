package com.example.i_commerce.domain.product.fixture;

import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.enums.ProductItemStatus;
import com.example.i_commerce.domain.product.entity.ProductOptionValue;
import java.util.List;

public class ProductItemFixture {

    public static ProductItem.ProductItemBuilder defaultProductItem() {
        return ProductItem
            .builder()
            .sku("SKU")
            .displayOptionName("기본 옵션 이름")
            .status(ProductItemStatus.ON_SALE)
            .attributes(List.of())
            .isDefault(true);

    }

    public static ProductItem mockProductItem(Long productItemId) {
        return defaultProductItem()
            .sku("SKU-00" + productItemId)
            .price(10000)
            .build();
    }

    public static ProductItem createDoubleOptionItem(
        ProductOptionValue ov1,
        ProductOptionValue ov2
    ) {
        return defaultProductItem()
            .optionValue1(ov1)
            .optionValue2(ov2)
            .build();
    }

    public static ProductItem createSingleOptionItem(
        ProductOptionValue ov1
    ) {
        return defaultProductItem()
            .optionValue1(ov1)
            .build();
    }

}

