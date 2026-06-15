package com.example.i_commerce.domain.product.exception;

import com.example.i_commerce.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProductErrorCode implements ErrorCode {

    // 400 BadRequest
    SEARCH_KEYWORD_TOO_SHORT(HttpStatus.BAD_REQUEST, "PRD-40001", "검색어는 2글자 이상이어야 합니다."),
    EXCEEDED_MAX_OPTION(HttpStatus.BAD_REQUEST, "PRD-40002", "최대 옵션 수를 초과했습니다."),
    NOT_SUPPORTED_OPTION(HttpStatus.BAD_REQUEST, "PRD-40003", "지원하지 않는 옵션입니다."),
    NOT_SUPPORTED_ATTRIBUTE(HttpStatus.BAD_REQUEST, "PRD-40004", "지원하지 않는 속성입니다."),
    CATEGORY_DEPTH_EXCEEDED(HttpStatus.BAD_REQUEST, "PRD-40005", "카테고리 최대 깊이를 초과했습니다."),
    NEGATIVE_QUANTITY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PRD-40006", "재고 수량은 음수일 수 없습니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "PRD-40007", "현재 상태에서 허용되지 않는 상태 변경입니다."),
    INVALID_OPTION_COUNT(HttpStatus.BAD_REQUEST, "PRD-40008", "옵션 개수가 올바르지 않습니다."),
    DUPLICATED_OPTION(HttpStatus.BAD_REQUEST, "PRD-40009", "중복 요청된 옵션이 존재합니다."),

    // 403 Forbidden
    GUEST_PAGE_LIMIT_EXCEEDED(HttpStatus.FORBIDDEN, "PRD-40301", "비로그인 사용자는 더 이상 페이지를 조회할 수 없습니다."),
    PRODUCT_ACCESS_DENIED(HttpStatus.FORBIDDEN, "PRD-40302", "해당 상품에 대한 접근 권한이 없습니다."),


    // 404 NotFound
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40401", "상품 정보를 찾을 수 없습니다."),
    PRODUCT_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40402", "해당 상품 아이템을 찾을 수 없습니다."),
    DEFAULT_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40403", "기본 상품을 찾을 수 없습니다."),
    OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40404","옵션이 존재하지 않습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40405", "카테고리가 존재하지 않습니다."),
    ATTRIBUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40406","속성이 존재하지 않습니다."),
    STOCK_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40407", "상품 재고가 존재하지 않습니다."),
    STOCK_HISTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40408", "재고 기록이 존재하지 않습니다."),
    CATEGORY_OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40409", "해당 카테고리의 옵션을 찾을 수 없습니다."),
    CATEGORY_ATTRIBUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40410", "해당 카테고리의 속성을 찾을 수 없습니다."),


    // 409 Conflict
    DUPLICATED_SKU(HttpStatus.CONFLICT, "PRD-40901", "SKU는 중복될 수 없습니다."),
    DUPLICATE_OPTION_NAME(HttpStatus.CONFLICT, "PRD-40902", "같은 이름의 옵션이 이미 존재합니다."),
    DUPLICATE_ATTRIBUTE_KEY(HttpStatus.CONFLICT, "PRD-40903", "속성 키가 이미 존재합니다."),
    DUPLICATE_CATEGORY_NAME(HttpStatus.CONFLICT, "PRD-40904", "이미 존재하는 카테고리입니다."),
    STOCK_ALREADY_INITIALIZED(HttpStatus.CONFLICT, "PRD-40905", "재고가 이미 초기화되었습니다."),
    INSUFFICIENT_STOCK(HttpStatus.CONFLICT, "PRD-40906", "재고가 충분하지 않습니다."),
    CATEGORY_HAS_PRODUCTS(HttpStatus.CONFLICT, "PRD-40907", "하위 상품이 존재하는 카테고리는 삭제할 수 없습니다."),
    STOCK_ALREADY_RESTORED(HttpStatus.CONFLICT, "PRD-40908", "이미 취소 처리된 주문입니다."),
    STOCK_UNAVAILABLE(HttpStatus.CONFLICT, "PRD-40909", "사용할 수 없는 재고입니다.")

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
