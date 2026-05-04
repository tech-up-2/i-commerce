package com.example.i_commerce.domain.product.entity;


import lombok.AllArgsConstructor;


@AllArgsConstructor
public enum StockChangeType {
    DEDUCT("주문으로 인한 차감"),
    RESTORE("주문 취소로 인한 복구"),
    ADJUST("관리자 수동 조정")
    ;

    private final String description;
}
