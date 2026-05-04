package com.example.i_commerce.domain.order.entity.emuns;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentStatus {
    READY("결제 대기", "결제 요청을 받아 대기 상태입니다."),
    PAID("결제 완료", "성공적으로 결제를 완료했습니다."),
    FAILED("결제 실패", "결제 과정에서 오류가 발생했습니다."),
    CANCELLED("결제 취소", "결제가 취소되었습니다.");

    private final String status;
    private final String description;
}
