package com.example.i_commerce.domain.order.event.listener;

import com.example.i_commerce.domain.order.entity.Delivery;
import com.example.i_commerce.domain.order.entity.emuns.DeliveryStatus;
import com.example.i_commerce.domain.order.event.dto.PaymentCompletedEvent;
<<<<<<<< HEAD:src/main/java/com/example/i_commerce/domain/order/event/listener/PaymentLogListener.java
import com.example.i_commerce.domain.order.service.PaymentHistoryService;
========
import com.example.i_commerce.domain.order.service.DeliveryService;
import com.example.i_commerce.domain.order.service.dto.DeliveryCancelRequestEvent;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
>>>>>>>> 13c6152 (feat: 결제 취소 요청시 배송 상태 변경 이벤트 처리):src/main/java/com/example/i_commerce/domain/order/event/listener/DeliveryListener.java
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
<<<<<<<< HEAD:src/main/java/com/example/i_commerce/domain/order/event/listener/PaymentLogListener.java
public class PaymentLogListener {
========
public class DeliveryListener {
>>>>>>>> 13c6152 (feat: 결제 취소 요청시 배송 상태 변경 이벤트 처리):src/main/java/com/example/i_commerce/domain/order/event/listener/DeliveryListener.java

    private final PaymentHistoryService paymentHistoryService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
<<<<<<<< HEAD:src/main/java/com/example/i_commerce/domain/order/event/listener/PaymentLogListener.java
    public void createPaymentHistoryRequest(PaymentCompletedEvent event) {
        paymentHistoryService.createPaymentHistory(event);
========
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        deliveryService.createDelivery(event);
>>>>>>>> 13c6152 (feat: 결제 취소 요청시 배송 상태 변경 이벤트 처리):src/main/java/com/example/i_commerce/domain/order/event/listener/DeliveryListener.java
    }

    @EventListener
    public void handleDeliveryCancelRequest(DeliveryCancelRequestEvent event) {
        deliveryService.cancelDelivery(event);

    }
}
