package com.example.i_commerce.domain.cart.exception;

import com.example.i_commerce.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CartErrorCode implements ErrorCode {

    EXCEED_STOCK_QUANTITY(HttpStatus.CONFLICT, "CRT-40901", "재고를 초과했습니다."),
    PRODUCT_NOT_AVAILABLE(HttpStatus.CONFLICT, "CRT-40902", "사용할 수 없는 상품입니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
