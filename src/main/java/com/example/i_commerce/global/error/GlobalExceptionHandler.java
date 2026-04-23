package com.example.i_commerce.global.error;

import com.example.i_commerce.global.common.response.ApiResponse;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ApiResponse<?> appExceptionHandler(AppException e) {
        return ApiResponse.error(e.getErrorCode(), e.getMessage());
    }
}
