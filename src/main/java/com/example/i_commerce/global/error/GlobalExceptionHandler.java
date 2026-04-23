package com.example.i_commerce.global.error;

import java.util.HashMap;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> appExceptionHandler(AppException e) {
        Map<String, Object> result = new HashMap<>();
        result.put("errorCode", e.getErrorCode().name());
        result.put("message", e.getMessage());

        return ResponseEntity.status(e.getErrorCode().getHttpStatus())
                .body(result);
    }
}
