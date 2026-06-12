package com.example.i_commerce.domain.order.support;


import com.example.i_commerce.domain.order.event.dto.DeliveryStatusChangedEvent;
import com.example.i_commerce.domain.order.service.SellerDeliveryService;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
    public Executor courierThreadPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(30);
        executor.setMaxPoolSize(30);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("mock-courier-thread-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
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
                long randomSleep = (long) (Math.random() * 500) + 500;
                Thread.sleep(randomSleep);

                log.info("====== [가상 택배사] 배송 ID {} 배송 완료 처리 진행 ======", deliveryId);
                 sellerDeliveryService.completeDelivery(orderId, deliveryId);

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
    }
}
