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

    // --- Product 도메인 (PRD) ---
    DUPLICATED_SKU(HttpStatus.CONFLICT, "PRD-40901", "SKU는 중복될 수 없습니다."),
    INVALID_OPTION(HttpStatus.CONFLICT, "PRD-40902", "요청한 옵션 값이 유효하지 않습니다."),
    EXCEEDED_MAX_OPTION(HttpStatus.CONFLICT, "PRD-40903", "최대 옵션 수를 초과했습니다."),
    CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40403", "카테고리가 존재하지 않습니다."),
    NOT_SUPPORTED_ATTRIBUTE(HttpStatus.CONFLICT, "PRD-40904", "지원하지 않는 속성입니다."),
    ATTRIBUTE_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40405","속성이 존재하지 않습니다."),
    OPTION_NOT_FOUND(HttpStatus.NOT_FOUND, "PRD-40406","옵션이 존재하지 않습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}