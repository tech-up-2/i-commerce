package com.example.i_commerce.domain.order.service.dto;

import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.Payment;
import lombok.Builder;

@Builder
public record PaymentDetailResponse(
        String orderId,
        String customerKey,
//        Long paymentId,
        Integer amount,
        String orderName,
        String customerName,
        String customerMobilePhone



) {
    public static PaymentDetailResponse of(Payment payment, Order order, String firstProductName) {
        return PaymentDetailResponse.builder()
                .orderId("PAYMENT_" + order.getId() + "_" +System.currentTimeMillis())
                .customerKey("USER_ID_" + order.getUserId())
//                .paymentId(payment.getId())
                .amount(order.getTotalPayAmount())
                .orderName(firstProductName + "항목 외 " + (order.getOrderProducts().size() - 1) + "건")
                .customerName(order.getReceiverName())
                .customerMobilePhone(order.getReceiverPhone())
                .build();

    }
}
