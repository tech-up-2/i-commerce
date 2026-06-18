package com.example.i_commerce.domain.product.fixture;

import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductAttribute;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.enums.ProductItemStatus;
import java.util.ArrayList;
import java.util.UUID;

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

    public static ProductItem createProductItemBy(
        Product product,
        int price,
        ProductItemStatus status
    ) {
        ProductItem item = ProductItem.builder()
            .sku(UUID.randomUUID().toString())
            .price(price)
            .status(status)
            .isDefault(true)
            .build();
        product.addItem(item);
        return item;
    }

    public static void createProductAttributeBy(
        ProductItem productItem,
        Long attributeId,
        String displayName
    ) {
        ProductAttribute attribute = ProductAttribute.builder()
            .attributeId(attributeId)
            .displayName(displayName)
            .displayOrder(1)
            .build();
        productItem.addAttribute(attribute);
    }

}

