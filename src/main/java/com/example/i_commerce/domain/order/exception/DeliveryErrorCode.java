package com.example.i_commerce.domain.order.exception;

import com.example.i_commerce.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum DeliveryErrorCode implements ErrorCode {

    CANNOT_SHIP_STATUS(HttpStatus.BAD_REQUEST, "DEV-40001", "배송 준비중(PREPARING) 상태의 상품만 배송 처리할 수 있습니다."),
    INVALID_DELIVERY_ORDER(HttpStatus.BAD_REQUEST, "DEV-40002", "해당 배송 정보와 일치하지 않는 주문 번호입니다."),

    STORE_FORBIDDEN(HttpStatus.FORBIDDEN, "DEV-40301", "입력한 정보를 확인해주세요."),

    DELIVERY_NOT_FOUND(HttpStatus.NOT_FOUND, "DEV-40401", "배송 정보를 찾을 수 없습니다.")
    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
