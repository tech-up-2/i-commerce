package com.example.i_commerce.domain.order.service;

import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.PaymentHistory;
import com.example.i_commerce.domain.order.event.dto.PaymentStatusChangedEvent;
import com.example.i_commerce.domain.order.exception.PaymentErrorCode;
import com.example.i_commerce.domain.order.repository.PaymentHistoryRepository;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.global.exception.AppException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentHistoryService {

    private final PaymentRepository paymentRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;

    @Transactional
    public void createPaymentHistory(PaymentStatusChangedEvent event) {
        Payment payment = paymentRepository.findById(event.paymentId()).orElseThrow(() -> new AppException(
                PaymentErrorCode.PAYMENT_NOT_FOUND));

        Order order = payment.getOrder();
        paymentHistoryRepository.save(
                PaymentHistory.builder()
                        .payment(payment)
                        .previousStatus(event.previousStatus())
                        .currentStatus(event.currentStatus())
                        .pgTid(event.pgTid())
                        .amount(payment.getAmount())
                        .reason(event.reason())
//                        .rawData(event.rawData())
                        .actorId(order.getUserId())
                        .build()
        );
    }

}
