package com.example.i_commerce.domain.order.exception;

import com.example.i_commerce.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    ORDER_NOT_OWNED(HttpStatus.FORBIDDEN, "ORD-40301", "해당 주문에 대한 권한이 없습니다."),

    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORD-40401", "order를 찾을 수 없습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
