package com.example.i_commerce.domain.product.fixture;

import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.enums.ProductItemStatus;
import java.util.ArrayList;

public class ProductItemFixture {

    public static ProductItem.ProductItemBuilder defaultProductItem() {
        return ProductItem
            .builder()
            .sku("SKU-001")
            .price(10000)
            .status(ProductItemStatus.ON_SALE)
            .attributes(new ArrayList<>())
            .isDefault(true);

    }

    public static ProductItem createItem(Long id, Boolean isDefault) {
        return defaultProductItem()
            .id(id)
            .isDefault(isDefault)
            .build();
    }

    public static ProductItem createItemWithStock(
        Product product, String sku, int stockQuantity
    ) {
        ProductItem item = defaultProductItem()
            .sku(sku)
            .build();
        product.addItem(item);
        item.initStock(stockQuantity);
        return item;
    }

}

