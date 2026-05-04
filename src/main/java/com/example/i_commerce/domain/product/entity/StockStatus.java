package com.example.i_commerce.domain.product.entity;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum StockStatus {
    IN_STOCK("재고 있음"),
    OUT_OF_STOCK("재고 없음")
    ;

    private final String description;
}
