package com.example.i_commerce.domain.product.entity.enums;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum OptionInputType {
    SELECT("셀렉트박스"),
    RADIO("라디오버튼");

    private final String description;

}
