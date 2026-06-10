package com.example.i_commerce.domain.order.event.listener;

import com.example.i_commerce.domain.order.event.dto.DeliveryStatusChangedEvent;
import com.example.i_commerce.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class OrderListener {

    private final OrderService orderService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleDeliveryStatusChanged(DeliveryStatusChangedEvent event) {
        orderService.updateOrderStatusByDeliveries(event.orderId());
    }

}
