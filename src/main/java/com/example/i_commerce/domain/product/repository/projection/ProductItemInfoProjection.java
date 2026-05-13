package com.example.i_commerce.domain.product.repository.projection;

import com.example.i_commerce.domain.product.entity.ProductItemStatus;

public interface ProductItemInfoProjection {
    Long getProductItemId();
    String getProductName();
    Integer getPrice();
    String getDisplayOptionName();
    Integer getStockQuantity();
    ProductItemStatus getStatus();
}
