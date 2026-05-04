package com.example.i_commerce.domain.order.event.listener;

import com.example.i_commerce.domain.order.event.dto.PaymentCompletedEvent;
import com.example.i_commerce.domain.order.service.DeliveryService;
import com.example.i_commerce.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCompletedOrderListener {

    private OrderService orderService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void updateOrderStatus(PaymentCompletedEvent event) {
        log.info("결제 완료 이벤트를 읽었습니다.");
        orderService.updateOrder(event.orderId());
        log.info("주문 상태가 업데이트 되었습니다.");

    }


}
