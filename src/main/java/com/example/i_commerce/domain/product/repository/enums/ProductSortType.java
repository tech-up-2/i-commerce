package com.example.i_commerce.domain.product.repository.enums;


import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum ProductSortType {

    RELEVANCE("관련도순"),
    PRICE_ASC("낮은 가격순"),
    PRICE_DESC("높은 가격순"),
    LATEST("최신 등록순");

    private final String description;
}
