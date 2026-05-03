package com.example.i_commerce.domain.product.fixture;

import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.Stock;
import com.example.i_commerce.domain.product.entity.StockChangeType;
import com.example.i_commerce.domain.product.entity.StockHistory;
import com.example.i_commerce.domain.product.entity.StockStatus;


public class StockFixture {


    public static Stock.StockBuilder defaultStock() {
        return Stock.builder()
            .status(StockStatus.IN_STOCK);
    }

    public static Stock mockStock(Long productItemId, int quantity) {
        ProductItem item = ProductItem.builder()
            .id(productItemId)
            .build();
        return defaultStock()
            .productItem(item)
            .quantity(quantity)
            .build();
    }

    public static Stock mockOutOfStock(Long productItemId) {
        ProductItem item = ProductItem.builder()
            .id(productItemId)
            .build();
        return defaultStock()
            .productItem(item)
            .quantity(0)
            .status(StockStatus.OUT_OF_STOCK)
            .build();
    }

    public static StockHistory mockDeductHistory(Stock stock, int quantity, Long orderId) {
        return StockHistory.builder()
            .stock(stock)
            .changeType(StockChangeType.DEDUCT)
            .changeQuantity(quantity)
            .orderId(orderId)
            .build();
    }

}



