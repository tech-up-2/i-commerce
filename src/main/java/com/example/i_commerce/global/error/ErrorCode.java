package com.example.i_commerce.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // --- 공통 에러 (COM) ---
    INVALID_PERMISSION(HttpStatus.UNAUTHORIZED, "COM-40101", "권한이 없습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COM-50001", "서버 내부 에러가 발생했습니다."),

    // --- Member 도메인 (USR) ---
    DUPLICATED_USER_NAME(HttpStatus.CONFLICT, "USR-40901", "이미 존재하는 유저명입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "USR-40101", "패스워드가 틀렸습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USR-40401", "존재하지 않는 회원입니다."),
    DEFAULT_ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "USR-40402", "기본 배송지가 설정되지 않았습니다."),


    // --- Post/Product 도메인 (PRD) ---
    // 게시글과 상품을 PRD(Product) 도메인으로 통합하여 구성했습니다.
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40401", "해당 게시글을 찾을 수 없습니다."),
    ALREADY_LIKED(HttpStatus.CONFLICT, "PRD-40901", "이미 좋아요를 누른 게시글입니다."),
    PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40402", "상품 정보를 찾을 수 없습니다."),



    // --- Order/Payment 도메인 (ORD) ---
    ORDER_TEMP_ERROR(HttpStatus.NOT_FOUND, "ORD-40400", "order도메인에서 발생하는 임시 에러입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}