package com.example.i_commerce.domain.product.entity.enums;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum StockStatus {
    IN_STOCK("재고 있음"),
    OUT_OF_STOCK("재고 없음"),
    UNAVAILABLE("사용할 수 없음")
    ;

    private final String description;
}
