package com.example.i_commerce.domain.order.service;

import com.example.i_commerce.domain.order.entity.Delivery;
import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.emuns.DeliveryStatus;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.order.entity.emuns.PaymentStatus;
import com.example.i_commerce.domain.order.event.dto.DeliveryCancelRequestEvent;
import com.example.i_commerce.domain.order.event.dto.PaymentApprovedEvent;
import com.example.i_commerce.domain.order.event.dto.PaymentStatusChangedEvent;
import com.example.i_commerce.domain.order.exception.PaymentErrorCode;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.domain.order.service.dto.PaymentCancelRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentConfirmRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentDetailResponse;
import com.example.i_commerce.domain.product.facade.StockFacade;
import com.example.i_commerce.global.exception.AppException;
import jakarta.annotation.PostConstruct;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final RestTemplate restTemplate;
    private final StockFacade stockFacade;

    @Value("${toss.secretKey}")
    private String SECRET_KEY;
    private String encodedKey;

    @PostConstruct
    public void init() {
        this.encodedKey = Base64.getEncoder().encodeToString((SECRET_KEY + ":").getBytes());
    }

    @Transactional(readOnly = true)
    public PaymentDetailResponse getPaymentDetails(Long userId, Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        Order order = payment.getOrder();

        if(!(userId).equals(order.getUserId())) {
            throw new AppException(PaymentErrorCode.ACCESS_DENIED);
        }

        if(payment.getPayStatus() != PaymentStatus.READY) {
            throw new AppException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }


        String firstProductName = order.getOrderProducts().getFirst().getProductName();

        return PaymentDetailResponse.of(payment, order, firstProductName);
    }

    @Transactional
    public void confirmPayment(PaymentConfirmRequest dto) {
        if (dto.tossOrderId() == null || !dto.tossOrderId().contains("_")) {
            throw new AppException(PaymentErrorCode.INVALID_PAYMENT_REQUEST);
        }
        Long paymentId = Long.valueOf(dto.tossOrderId().split("_")[1]);
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        if(payment.getPayStatus() != PaymentStatus.READY) {
            throw new AppException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }

        if (!payment.getAmount().equals(dto.amount())) {
            throw new AppException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> params = new HashMap<>();
        params.put("orderId", dto.tossOrderId());
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

                publisher.publishEvent(new PaymentApprovedEvent(payment.getOrder().getId()));

                publisher.publishEvent(new PaymentStatusChangedEvent(
                        payment,
                        previousStatus,
                        "결제 완료",
                        PaymentStatus.PAID,
                        pgTid,
                        response.getBody().toString()));
            }
        } catch (Exception e) {
            // TODO: 외부 API가 응답하지 않거나, 응답이 예상과 다를 때의 예외 처리 로직 보완
            log.info("something wrong");
            payment.changePayStatus(PaymentStatus.FAILED);
            throw new AppException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
        }
    }

    @Transactional
    public void cancelPayment(PaymentCancelRequest dto) {

        Payment payment = paymentRepository.findById(dto.paymentId())
                .orElseThrow(() -> new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        if(!Objects.equals(dto.paymentKey(), payment.getPgTid())) {
            throw new AppException(PaymentErrorCode.INVALID_PAYMENT_KEY);
        }

        if(payment.getPayStatus() == PaymentStatus.CANCELLED) {
            throw new AppException(PaymentErrorCode.PAYMENT_ALREADY_CANCELLED);
        }

        if(dto.cancelAmount() > payment.getCancelableAmount()) {
            throw new AppException(PaymentErrorCode.INVALID_CANCEL_AMOUNT);
        }

        publisher.publishEvent(new DeliveryCancelRequestEvent(payment.getOrder().getId()));

        Order order = payment.getOrder();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Basic " + encodedKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> params = new HashMap<>();
        params.put("cancelReason", dto.cancelReason());
        params.put("cancelAmount", dto.cancelAmount());

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(params, headers);

        try{
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://api.tosspayments.com/v1/payments/" + dto.paymentKey() + "/cancel",
                    entity,
                    Map.class
            );

            if(response.getStatusCode() == HttpStatus.OK) {
                PaymentStatus previousStatus = payment.getPayStatus();
                String pgTid = (String) response.getBody().get("paymentKey");

                payment.cancelPayment(dto.cancelAmount());
                order.changeOrderStatus(OrderStatus.CANCELLED);

                stockFacade.rollbackStocks(order.getId());

                publisher.publishEvent(new PaymentStatusChangedEvent(
                        payment,
                        previousStatus,
                        dto.cancelReason(),
                        PaymentStatus.CANCELLED,
                        pgTid,
                        response.getBody().toString()));
            } else {
                throw new AppException(PaymentErrorCode.PAYMENT_CANCEL_FAILED);
            }

        } catch (Exception e) {
            log.info("something wrong");
            throw new AppException(PaymentErrorCode.PAYMENT_CANCEL_FAILED);
        }

    }
}
