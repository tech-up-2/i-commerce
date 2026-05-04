package com.example.i_commerce.domain.product.fixture;

import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.ProductItemStatus;

public class ProductItemFixture {

    public static ProductItem.ProductItemBuilder defaultProductItem() {
        return ProductItem
            .builder()
            .displayOptionName("기본 옵션 이름")
            .status(ProductItemStatus.ON_SALE)
            .isDefault(false);

    }

    public static ProductItem mockProductItem(Long productItemId) {
        return defaultProductItem()
            .sku("SKU-00" + productItemId)
            .price(10000)
            .isDefault(true)
            .build();
    }
}

