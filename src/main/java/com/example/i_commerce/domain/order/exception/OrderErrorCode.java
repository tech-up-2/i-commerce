package com.example.i_commerce.domain.order.exception;

import com.example.i_commerce.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {
    ORDER_TEMP_ERROR(HttpStatus.NOT_FOUND, "ORD-40400", "order도메인에서 발생하는 임시 에러입니다."),
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
