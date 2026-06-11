package com.example.i_commerce.domain.order.support;


import com.example.i_commerce.domain.order.event.dto.DeliveryStatusChangedEvent;
import com.example.i_commerce.domain.order.service.SellerDeliveryService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("k6")
public class MockCourierComponent {

    private final SellerDeliveryService sellerDeliveryService;

    // 부하 테스트 시 결제/배송시작 API 스레드를 방해하지 않기 위한 독립 스레드 풀
    @Bean(name = "courierThreadPool")
    public ExecutorService courierThreadPool() {
        return Executors.newFixedThreadPool(30, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("mock-courier-thread-" + thread.getName());
            return thread;
        });
    }

    @Async("courierThreadPool")
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            condition = "#event.currentStatus.name() == 'SHIPPING'"
    )
    public void onDeliveryStatusChanged(DeliveryStatusChangedEvent event) {
        Long orderId = event.orderId();
        Long deliveryId = event.deliveryId();
            try {
                Thread.sleep(3000);

                log.info("====== [가상 택배사] 배송 ID {} 배송 완료 처리 진행 ======", deliveryId);
                 sellerDeliveryService.completeDelivery(orderId, deliveryId);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
    }
}
