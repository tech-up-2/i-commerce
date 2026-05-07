package com.example.i_commerce.domain.order.service;

import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.emuns.DeliveryStatus;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.order.entity.emuns.PaymentStatus;
import com.example.i_commerce.domain.order.event.dto.PaymentCompletedEvent;
import com.example.i_commerce.domain.order.exception.PaymentErrorCode;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.domain.order.service.dto.PaymentConfirmRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentDetailResponse;
import com.example.i_commerce.global.exception.AppException;
import jakarta.annotation.PostConstruct;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher publisher;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${toss.secretKey}")
    private String SECRET_KEY;
    private String encodedKey;

    @PostConstruct
    public void init() {
        this.encodedKey = Base64.getEncoder().encodeToString((SECRET_KEY + ":").getBytes());
    }


    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentDetails(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        if(payment.getPayStatus() != PaymentStatus.READY) {
            throw new AppException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }

        Order order = payment.getOrder();
        String firstProductName = order.getOrderProducts().getFirst().getProductName();

        return PaymentDetailResponse.of(payment, order, firstProductName);
    }

    @Transactional
    public void confirmPayment(PaymentConfirmRequest dto) {
        Long paymentId = Long.valueOf(dto.orderId().split("_")[1]);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getAmount().equals(dto.amount())) {
            throw new AppException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> params = new HashMap<>();
        params.put("orderId", dto.orderId());
        params.put("amount", dto.amount());
        params.put("paymentKey", dto.paymentKey());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.tosspayments.com/v1/payments/confirm",
                    entity,
                    Map.class
            );
            log.info(String.valueOf(response));

            if (response.getStatusCode() == HttpStatus.OK) {
                PaymentStatus previousStatus = payment.getPayStatus();
                String pgTid = (String) response.getBody().get("paymentKey");

                payment.completePayment(pgTid);
                payment.getOrder().changeOrderStatus(OrderStatus.CONFIRMED);
                payment.getOrder().getDeliveries().forEach(delivery -> delivery.changeDeliveryStatus(DeliveryStatus.PREPARING));

                publisher.publishEvent(new PaymentCompletedEvent(
                        payment,
                        previousStatus,
                        "결제 완료",
                        PaymentStatus.PAID,
                        pgTid,
                        response.getBody().toString()));
            }
        } catch (Exception e) {
            log.info("something wrong");
            payment.changePayStatus(PaymentStatus.FAILED);
            throw new AppException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        }
    }


}
