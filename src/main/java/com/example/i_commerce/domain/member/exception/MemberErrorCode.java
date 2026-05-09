package com.example.i_commerce.domain.member.exception;

import com.example.i_commerce.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {

    // --- 공통 에러 (COM) ---
//    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COM-40001", "잘못된 입력값입니다."),
//    INVALID_PERMISSION(HttpStatus.UNAUTHORIZED, "COM-40101", "권한이 없습니다."),
//    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COM-50001", "서버 내부 에러가 발생했습니다."),

    // --- Member 도메인 (USR) ---
    // --- 400
    INVALID_SIGNUP_REQUEST(HttpStatus.BAD_REQUEST,
        "USR-40001", "회원가입 요청값이 올바르지 않습니다."),
    INACTIVE_MEMBER(HttpStatus.BAD_REQUEST, "USR-40002", "휴면 상태인 계정입니다."),
    WITHDRAWN_MEMBER(HttpStatus.BAD_REQUEST, "USR-40003", "탈퇴 처리된 계정입니다."),
    DELIVERY_ADDRESS_LIMIT_EXCEEDED(
        HttpStatus.BAD_REQUEST, "USR-40004", "배송지는 최대 5개까지 등록할 수 있습니다."),

    // --- 401
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "USR-40101", "패스워드가 틀렸습니다."),

    // --- 404
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USR-40401", "존재하지 않는 회원입니다."),
    DEFAULT_ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND,
        "USR-40402", "기본 배송지가 설정되지 않았습니다."),
    SELLER_NOT_FOUND(HttpStatus.NOT_FOUND, "USR-40403", "존재하지 않는 판매자입니다."),
    DELIVERY_ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND,
        "USR-40404", "배송지가 존재하지 않습니다."),

    // --- 409
    DUPLICATED_EMAIL(HttpStatus.CONFLICT, "USR-40901", "이미 가입된 이메일입니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
