package com.example.i_commerce.domain.order.event.listener;

import com.example.i_commerce.domain.order.event.dto.PaymentApprovedEvent;
import com.example.i_commerce.domain.order.service.DeliveryService;
import com.example.i_commerce.domain.order.event.dto.DeliveryCancelRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeliveryListener {

    private final DeliveryService deliveryService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompleted(PaymentApprovedEvent event) {
        deliveryService.createDelivery(event);
    }

    @EventListener
    public void handleDeliveryCancelRequest(DeliveryCancelRequestEvent event) {
        deliveryService.cancelDelivery(event);

    }
}
