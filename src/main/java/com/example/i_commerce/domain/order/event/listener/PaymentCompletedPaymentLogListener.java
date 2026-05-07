package com.example.i_commerce.domain.order.event.listener;

import com.example.i_commerce.domain.order.event.dto.PaymentCompletedEvent;
import com.example.i_commerce.domain.order.service.DeliveryService;
import com.example.i_commerce.domain.order.service.PaymentHistoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCompletedPaymentLogListener {

    private final PaymentHistoryService paymentHistoryService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createPaymentHistoryRequest(PaymentCompletedEvent event) {
        paymentHistoryService.createPaymentHistory(event);
    }
}
