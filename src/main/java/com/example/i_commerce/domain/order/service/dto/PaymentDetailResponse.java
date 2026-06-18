package com.example.i_commerce.domain.order.service.dto;

import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.Payment;
import lombok.Builder;

@Builder
public record PaymentDetailResponse(
        String tossOrderId,
        String customerKey,
        Integer amount,
        String orderName,
        String customerName,
        String customerMobilePhone

) {
    public static PaymentDetailResponse of(Payment payment, Order order, String firstProductName) {
        return PaymentDetailResponse.builder()
                .tossOrderId(payment.getTossOrderId())
                .customerKey("USER_ID_" + order.getUserId())
                .amount(order.getTotalPayAmount())
                .orderName(firstProductName + "항목 외 " + (order.getOrderProducts().size() - 1) + "건")
                .customerName(order.getReceiverName())
                .customerMobilePhone(order.getReceiverPhone())
                .build();

    }
}
