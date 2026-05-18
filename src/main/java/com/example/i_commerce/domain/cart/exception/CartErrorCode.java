package com.example.i_commerce.domain.cart.exception;

import com.example.i_commerce.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CartErrorCode implements ErrorCode {

    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CRT-40401", "장바구니가 존재하지 않습니다."),
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "CRT-40402", "장바구니에 상품이 존재하지 않습니다."),

    EXCEED_STOCK_QUANTITY(HttpStatus.CONFLICT, "CRT-40901", "재고를 초과했습니다."),
    PRODUCT_NOT_AVAILABLE(HttpStatus.CONFLICT, "CRT-40902", "사용할 수 없는 상품입니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
