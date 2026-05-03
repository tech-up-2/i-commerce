package com.example.i_commerce.domain.product.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductItemStatus {
    ON_SALE("판매중", "판매 중인 상품입니다."),
    OUT_OF_STOCK("재고 없음", "현재 재고가 없는 상품입니다."),

    ;

    private final String status;
    private final String description;
}
