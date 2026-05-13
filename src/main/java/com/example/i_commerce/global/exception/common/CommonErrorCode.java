package com.example.i_commerce.global.exception.common;

import com.example.i_commerce.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ErrorCode {
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COM-40001", "잘못된 입력값입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COM-40301", "접근 권한이 없습니다."),
    INVALID_PERMISSION(HttpStatus.UNAUTHORIZED, "COM-40101", "권한이 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COM-40102", "로그인이 필요합니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COM-50001", "서버 내부 에러가 발생했습니다."),

    ;

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
