package com.example.i_commerce.domain.product.entity;


import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public enum ProductStatus {
    ON_SALE("판매중", "판매되고 있는 상품입니다."),
    PENDING("판매 대기", "판매 대기중인 상품입니다."),
    DISCONTINUED("판매 중단", "판매가 중단된 상품입니다.");

    private final String status;
    private final String description;
}
