package com.example.i_commerce.domain.order.event.listener;

import com.example.i_commerce.domain.order.event.dto.OrderCreatedEvent;
import com.example.i_commerce.domain.order.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderCreatedPaymentListener {

    private final PaymentService paymentService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void paymentRequest(OrderCreatedEvent event) {
        log.info("주문 생성 이벤트를 읽었습니다.");
        paymentService.pay(event);
        log.info("결제가 완료되었습니다.");

    }

}
