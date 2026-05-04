package com.example.i_commerce.domain.order.event.listener;

import com.example.i_commerce.domain.order.event.dto.PaymentCompletedEvent;
import com.example.i_commerce.domain.order.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCompletedDeliveryListener {

    private final DeliveryService deliveryService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createDeliveryRequest(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트를 읽었습니다.");
        deliveryService.createDelivery(event);
        log.info("배송 객체가 생성되었습니다.");

    }
}
