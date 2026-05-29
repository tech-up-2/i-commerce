package com.example.i_commerce.global.exception;

import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.exception.common.CommonErrorCode;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<?>> appExceptionHandler(AppException e) {
        return ResponseEntity
            .status(e.getErrorCode().getHttpStatus())
            .body(ApiResponse.error(e.getErrorCode(), e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)//@Valid로 인해 발생하는 예외처리
    public ResponseEntity<ApiResponse<?>> validationExceptionHandler(
        MethodArgumentNotValidException e
    ) {
        Map<String, String> errors = new HashMap<>();

        e.getBindingResult()
            .getFieldErrors()
            .forEach(error -> errors.put(
                error.getField(),
                error.getDefaultMessage()
            ));

        return ResponseEntity
            .badRequest()
            .body(ApiResponse.error(CommonErrorCode.INVALID_INPUT_VALUE, errors.toString()));
    }

    @ExceptionHandler({//권한 거부 관련 예외 따로 처리
        AccessDeniedException.class,
        AuthorizationDeniedException.class
    })
    public ResponseEntity<ApiResponse<?>> accessDeniedExceptionHandler(Exception e) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(ApiResponse.error(CommonErrorCode.FORBIDDEN, "접근 권한이 없습니다."));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleAllException(Exception e) {
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR, e.getMessage()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleConstraintViolationException(
        ConstraintViolationException e) {

        String errorMessage = e.getConstraintViolations().iterator().next().getMessage();

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.error(CommonErrorCode.INVALID_INPUT_VALUE, e.getMessage()));
    }
}
