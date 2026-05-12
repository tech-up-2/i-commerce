package com.example.i_commerce.domain.order.exception;

import com.example.i_commerce.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "PAY-40401", "결제 정보를 찾을 수 없습니다."),
    INVALID_PAYMENT_AMOUNT(HttpStatus.BAD_REQUEST, "PAY-40001", "결제 금액이 일치하지 않습니다."),
    PAYMENT_CONFIRM_FAILED(HttpStatus.BAD_REQUEST, "PAY-40002", "결제 승인 처리에 실패했습니다."),
    INVALID_PAYMENT_STATUS(HttpStatus.BAD_REQUEST, "PAY-40003", "결제 가능한 상태가 아닙니다."),
    PAYMENT_CANCEL_FAILED(HttpStatus.BAD_REQUEST, "PAY-40004","결제 취소 처리에 실패했습니다."),
    INVALID_PAYMENT_KEY(HttpStatus.BAD_REQUEST, "PAY-40005", "paymentKey가 일치하지 않습니다."),
    INVALID_CANCEL_AMOUNT(HttpStatus.BAD_REQUEST, "PAY-40006", "취소 금액이 일치하지 않습니다."),
    PAYMENT_ALREADY_CANCELLED(HttpStatus.BAD_REQUEST, "PAY-40007", "이미 취소된 결제입니다."),
    PAYMENT_CANCEL_IMPOSSIBLE_ALREADY_SHIPPED(HttpStatus.BAD_REQUEST, "PAY-40008", "이미 상품이 출고(배송)되어 결제 취소가 불가능합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
