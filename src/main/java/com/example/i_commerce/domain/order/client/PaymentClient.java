package com.example.i_commerce.domain.order.client;

import com.example.i_commerce.domain.order.service.dto.PaymentCancelRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentConfirmRequest;
import java.util.Map;

public interface PaymentClient {
    Map<String, Object> requestConfirm(PaymentConfirmRequest dto);
    Map<String, Object> requestCanceled(PaymentCancelRequest dto);
    Map<String, Object> checkPaymentStatus(String paymentKey);
}
