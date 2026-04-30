package com.example.i_commerce.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // --- 공통 에러 (COM) ---
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COM-40001", "잘못된 입력값입니다."),
    INVALID_PERMISSION(HttpStatus.UNAUTHORIZED, "COM-40101", "권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COM-50001", "서버 내부 에러가 발생했습니다."),

    // --- Member 도메인 (USR) ---
    DUPLICATED_USER_NAME(HttpStatus.CONFLICT, "USR-40901", "이미 존재하는 유저명입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "USR-40101", "패스워드가 틀렸습니다."),
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "USR-40902", "이미 가입된 이메일입니다."),
    INVALID_SIGNUP_REQUEST(HttpStatus.BAD_REQUEST, "USR-40001", "회원가입 요청값이 올바르지 않습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "USR-40401", "존재하지 않는 회원입니다."),

    // --- Post/Product 도메인 (PRD) ---
    // 게시글과 상품을 PRD(Product) 도메인으로 통합하여 구성했습니다.
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40401", "해당 게시글을 찾을 수 없습니다."),
    ALREADY_LIKED(HttpStatus.CONFLICT, "PRD-40901", "이미 좋아요를 누른 게시글입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USR-40401", "존재하지 않는 회원입니다."),
    DEFAULT_ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "USR-40402", "기본 배송지가 설정되지 않았습니다."),

    // --- Product 도메인 (PRD) ---
    DUPLICATED_SKU(HttpStatus.CONFLICT, "PRD-40901", "SKU는 중복될 수 없습니다."),
    NEGATIVE_QUANTITY_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "STK-40003", "재고 수량은 음수일 수 없습니다."),
    STOCK_ALREADY_INITIALIZED(HttpStatus.CONFLICT, "STK-40903", "재고가 이미 초기화되었습니다."),
    INVALID_OPTION(HttpStatus.CONFLICT, "PRD-40902", "요청한 옵션 값이 유효하지 않습니다."),
    EXCEEDED_MAX_OPTION(HttpStatus.CONFLICT, "PRD-40903", "최대 옵션 수를 초과했습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40403", "카테고리가 존재하지 않습니다."),
    NOT_SUPPORTED_ATTRIBUTE(HttpStatus.CONFLICT, "PRD-40904", "지원하지 않는 속성입니다."),
    ATTRIBUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40405","속성이 존재하지 않습니다."),
    OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40406","옵션이 존재하지 않습니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40402", "상품 정보를 찾을 수 없습니다."),

    // --- Order/Payment 도메인 (ORD) ---
    ORDER_TEMP_ERROR(HttpStatus.NOT_FOUND, "ORD-40400", "order도메인에서 발생하는 임시 에러입니다."),

    // --- Review 도메인 (REV) ---
    ALREADY_REVIEWED(HttpStatus.CONFLICT, "REV-40901", "이미 리뷰를 작성한 상품입니다."),
    ALREADY_COMMENTED(HttpStatus.CONFLICT, "REV-40902", "이미 답글을 작성한 상품입니다."),
    INVALID_STAR_RATING(HttpStatus.BAD_REQUEST, "REV-40903", "별점은 1~5점 사이여야 합니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "REV-40904", "해당 리뷰를 찾을 수 없습니다.")

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}