package com.example.i_commerce.domain.order.service;

import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.PaymentHistory;
import com.example.i_commerce.domain.order.event.dto.PaymentStatusChangedEvent;
import com.example.i_commerce.domain.order.repository.PaymentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentHistoryService {

    private final PaymentHistoryRepository paymentHistoryRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createPaymentHistory(PaymentStatusChangedEvent event) {
        Payment payment = event.payment();
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
