package com.example.i_commerce.domain.product.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OptionType {

    NONE(0, "옵션 없음"),
    SINGLE(1, "옵션 1개"),
    DOUBLE(2, "옵션 2개");

    private final int code;
    private final String description;

}
