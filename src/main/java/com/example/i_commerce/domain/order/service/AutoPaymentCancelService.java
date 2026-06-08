package com.example.i_commerce.domain.order.service;

import com.example.i_commerce.domain.order.client.PaymentClient;
import com.example.i_commerce.domain.order.client.TossPaymentClient;
import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.order.entity.emuns.PaymentStatus;
import com.example.i_commerce.domain.order.event.dto.PaymentStatusChangedEvent;
import com.example.i_commerce.domain.order.exception.PaymentErrorCode;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.domain.order.service.dto.PaymentCancelRequest;
import com.example.i_commerce.global.exception.AppException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AutoPaymentCancelService {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher publisher;
    private final PaymentClient tossPaymentClient;


    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void autoCancelPayment(PaymentCancelRequest dto) {
        Payment payment = paymentRepository.findByTossOrderIdWithOrder(dto.tossOrderId())
                .orElseThrow(() -> new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        Order order = payment.getOrder();

        try {
            Map<String, Object> response = tossPaymentClient.requestCanceled(dto);

            payment.failByOutOfStock(dto.paymentKey());
            order.changeOrderStatus(OrderStatus.CANCELLED);

            publisher.publishEvent(new PaymentStatusChangedEvent(
                    payment, PaymentStatus.READY, dto.cancelReason(), PaymentStatus.FAILED, dto.paymentKey(), response.toString()));
        } catch (AppException e) {
            if (e.getErrorCode() == PaymentErrorCode.PAYMENT_NETWORK_TIMEOUT) {
                log.error("[자동 취소 실패] 자동 취소 시도 중 타임아웃 발생. 토스 장부 확인 불가.");
                payment.failByOutOfStock(dto.paymentKey());
                payment.prepareCancellation(dto.cancelAmount(), dto.cancelReason());
                order.changeOrderStatus(OrderStatus.CANCEL_REQUESTED);
            }
        }

    }


}
