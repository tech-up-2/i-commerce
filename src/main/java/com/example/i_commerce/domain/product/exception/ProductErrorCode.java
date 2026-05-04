package com.example.i_commerce.domain.product.exception;

import com.example.i_commerce.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    // NotFound
    OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40401","옵션이 존재하지 않습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40402", "상품 정보를 찾을 수 없습니다."),
    ATTRIBUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40403","속성이 존재하지 않습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40404", "카테고리가 존재하지 않습니다."),
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40405", "상품 재고가 존재하지 않습니다."),
    STOCK_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40406", "재고 기록이 존재하지 않습니다."),

    // Conflict
    NOT_SUPPORTED_ATTRIBUTE(HttpStatus.CONFLICT, "PRD-40904", "지원하지 않는 속성입니다."),
    DUPLICATED_SKU(HttpStatus.CONFLICT, "PRD-40901", "SKU는 중복될 수 없습니다."),
    NEGATIVE_QUANTITY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "STK-40003", "재고 수량은 음수일 수 없습니다."),
    STOCK_ALREADY_INITIALIZED(HttpStatus.CONFLICT, "STK-40903", "재고가 이미 초기화되었습니다."),
    INVALID_OPTION(HttpStatus.CONFLICT, "PRD-40902", "요청한 옵션 값이 유효하지 않습니다."),
    EXCEEDED_MAX_OPTION(HttpStatus.BAD_REQUEST, "PRD-40903", "최대 옵션 수를 초과했습니다."),

    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "PRD-40903", "재고가 충분하지 않습니다.")


    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
