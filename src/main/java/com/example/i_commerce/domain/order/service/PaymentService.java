package com.example.i_commerce.domain.order.service;

import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.emuns.PaymentStatus;
import com.example.i_commerce.domain.order.event.dto.OrderCreatedEvent;
import com.example.i_commerce.domain.order.event.dto.PaymentCompletedEvent;
import com.example.i_commerce.domain.order.exception.OrderErrorCode;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.global.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher publisher;

    public void pay(OrderCreatedEvent event) {
        //TODO : PG연결
        Payment payment = paymentRepository.findById(event.paymentId()).orElseThrow(() -> new AppException(OrderErrorCode.ORDER_TEMP_ERROR));
        payment.changePayStatus(PaymentStatus.PAID);

        publisher.publishEvent(new PaymentCompletedEvent(event.orderId(), event.paymentId(), event.userId()));

    }

}
