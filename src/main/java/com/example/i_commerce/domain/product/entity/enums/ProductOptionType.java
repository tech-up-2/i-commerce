package com.example.i_commerce.domain.product.entity.enums;

import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.global.exception.AppException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ProductOptionType {

    NONE(0, "옵션 없음"),
    SINGLE(1, "옵션 1개"),
    DOUBLE(2, "옵션 2개");

    private final int requiredOptionCount;
    private final String description;

    public void validateOptionCount(int count) {
        if (count != requiredOptionCount) {
            throw new AppException(ProductErrorCode.INVALID_OPTION_COUNT);
        }
    }

}
