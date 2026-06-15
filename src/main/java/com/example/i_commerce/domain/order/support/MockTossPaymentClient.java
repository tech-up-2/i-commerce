package com.example.i_commerce.domain.order.support;

import com.example.i_commerce.domain.order.client.PaymentClient;
import com.example.i_commerce.domain.order.service.dto.PaymentCancelRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentConfirmRequest;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Profile("k6")
@Component
public class MockTossPaymentClient implements PaymentClient {
    @Override
    public Map<String, Object> requestConfirm(PaymentConfirmRequest dto) {
        applyRandomDelay(100, 1500);

        applyRandomError(5);

        log.info("[Mock Toss API] 결제 승인 성공 - orderId: {}", dto.tossOrderId());
        Map<String, Object> response = new HashMap<>();
        response.put("paymentKey", dto.paymentKey());
        response.put("orderId", dto.tossOrderId());
        response.put("totalAmount", dto.amount());
        response.put("status", "DONE");
        response.put("approvedAt", OffsetDateTime.now().toString());
        return response;
    }

    @Override
    public Map<String, Object> requestCanceled(PaymentCancelRequest dto) {
        applyRandomDelay(50, 500);
        applyRandomError(10);

        log.info("[Mock Toss API] 결제 취소 성공 - paymentKey: {}", dto.paymentKey());
        Map<String, Object> response = new HashMap<>();
        response.put("paymentKey", dto.paymentKey());
        response.put("status", "CANCELED");
        return response;
    }

    @Override
    public Map<String, Object> checkPaymentStatus(String paymentKey) {
        log.info("[Mock Toss API] 단건 상태 조회 성공 - paymentKey: {}", paymentKey);
        return Map.of("paymentKey", paymentKey, "status", "DONE");
    }

    private void applyRandomDelay(int minMs, int maxMs) {
        int delay = ThreadLocalRandom.current().nextInt(minMs, maxMs + 1);
        try {
            log.debug("[Mock Toss API] 딜레이 적용중: {}ms", delay);
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Mock 서버 딜레이 중 인터럽트 발생", e);
        }
    }

    private void applyRandomError(int percentage) {
        int randomValue = ThreadLocalRandom.current().nextInt(100);
        if (randomValue < percentage) {
            log.warn("[Mock Toss API] 외부 API 장애 유도 (확률: {}%)", percentage);
            throw new WebClientResponseException(
                    500,
                    "Internal Server Error (Mock Generated)",
                    null, null, null
            );
        }
    }
}
