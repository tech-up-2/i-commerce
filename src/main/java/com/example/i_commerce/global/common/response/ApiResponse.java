package com.example.i_commerce.global.common.response;


import com.example.i_commerce.global.exception.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T> (
    String code,
    String message,
    T data
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>( "SUCCESS","API 요청에 성공했습니다", data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>("SUCCESS","API 요청에 성공했습니다", null);
    }

    public static ApiResponse<?> error(ErrorCode errorCode, String message) {
        return new ApiResponse<>(errorCode.getCode(), message, errorCode.getHttpStatus().toString());
    }
}
