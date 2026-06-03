package com.example.i_commerce.domain.order.service;

import com.example.i_commerce.domain.order.client.PaymentClient;
import com.example.i_commerce.domain.order.entity.Delivery;
import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.emuns.DeliveryStatus;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.order.entity.emuns.PaymentStatus;
import com.example.i_commerce.domain.order.event.dto.PaymentApprovedEvent;
import com.example.i_commerce.domain.order.event.dto.PaymentStatusChangedEvent;
import com.example.i_commerce.domain.order.exception.PaymentErrorCode;
import com.example.i_commerce.domain.order.repository.DeliveryRepository;
import com.example.i_commerce.domain.order.repository.OrderRepository;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.domain.order.service.dto.PaymentCancelPreparedDto;
import com.example.i_commerce.domain.order.service.dto.PaymentConfirmPrepareDto;
import com.example.i_commerce.domain.order.service.dto.PaymentCancelRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentConfirmRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentDetailResponse;
import com.example.i_commerce.domain.product.application.dto.StockDeductCommand;
import com.example.i_commerce.global.exception.AppException;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final DeliveryRepository deliveryRepository;
    private final ApplicationEventPublisher publisher;
    private final PaymentClient paymentClient;

    @Transactional
    public PaymentDetailResponse getPaymentDetails(Long userId, Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        if(!(userId).equals(order.getUserId())) {
            throw new AppException(PaymentErrorCode.ACCESS_DENIED);
        }

        if(!order.getOrderStatus().equals(OrderStatus.PENDING)) {
            throw new AppException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }

        List<Payment> readyPayments = order.getPayments().stream()
                .filter(payment -> Objects.equals(payment.getPayStatus(), PaymentStatus.READY))
                .sorted(Comparator.comparing(Payment::getId).reversed())
                .toList();

        Payment payment;
        if(readyPayments.isEmpty()) {

            payment = paymentRepository.save(Payment.builder()
                    .order(order)
                    .amount(order.getTotalPayAmount())
                    .cancelableAmount(0)
                    .payStatus(PaymentStatus.READY)
                    .build());

            order.getPayments().add(payment);
        } else {
            payment = readyPayments.getFirst();

            if(readyPayments.size() > 1) {
                for(int i = 1 ; i < readyPayments.size() ; i++) {
                    readyPayments.get(i).changePayStatus(PaymentStatus.FAILED);
                }
            }
        }

        String firstProductName = order.getOrderProducts().getFirst().getProductName();

        return PaymentDetailResponse.of(payment, order, firstProductName);
    }

    //-----------------------------------
    // 결제 - 에러 핸들링
    //-----------------------------------

    @Transactional(readOnly = true)
    public PaymentConfirmPrepareDto validateAndPrepareConfirm(PaymentConfirmRequest dto) {
        Payment payment = paymentRepository.findByTossOrderIdWithOrder(dto.tossOrderId())
                .orElseThrow(() -> new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        if (payment.getPayStatus() != PaymentStatus.READY) {
            throw new AppException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }
        if (!payment.getAmount().equals(dto.amount())) {
            throw new AppException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }

        Order order = payment.getOrder();
        List<StockDeductCommand> commands = order.getOrderProducts().stream()
                .map(op -> new StockDeductCommand(op.getProductSkuId(), op.getCount(), order.getId()))
                .toList();

        return PaymentConfirmPrepareDto.of(payment, order.getId(), commands);
    }

    @Transactional
    public void completePaymentSuccess(String tossOrderId, String pgTid, PaymentStatus previousStatus, String responseStr) {
        Payment payment = paymentRepository.findByTossOrderIdWithOrder(tossOrderId).orElseThrow(() -> new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        payment.completePayment(pgTid);
        payment.getOrder().changeOrderStatus(OrderStatus.CONFIRMED);

        publisher.publishEvent(new PaymentApprovedEvent(payment.getOrder().getId()));
        publisher.publishEvent(new PaymentStatusChangedEvent(payment, previousStatus, "결제 완료", PaymentStatus.PAID, pgTid, responseStr));
    }

    @Transactional
    public void completePaymentCancel(Long paymentId, PaymentStatus previousStatus, String pgTid, String responseStr) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow();
        publisher.publishEvent(new PaymentStatusChangedEvent(payment, previousStatus, "재고 부족으로 인한 자동 취소", PaymentStatus.FAILED, pgTid, responseStr));
    }

    @Transactional
    public void changeStatusToFailed(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow();
        payment.changePayStatus(PaymentStatus.FAILED);
    }

    @Transactional
    public void handleTimeoutSuccess(String tossOrderId) {
        Payment payment = paymentRepository.findByTossOrderIdWithOrder(tossOrderId).orElseThrow();
        payment.changePayStatus(PaymentStatus.UNKNOWN_HOLD);
        payment.getOrder().changeOrderStatus(OrderStatus.UNKNOWN_HOLD);
    }

    @Transactional
    public void handleTimeoutFailed(String tossOrderId) {
        Payment payment = paymentRepository.findByTossOrderIdWithOrder(tossOrderId).orElseThrow();
        payment.changePayStatus(PaymentStatus.FAILED);
        payment.getOrder().changeOrderStatus(OrderStatus.CANCELLED);
    }
    //-----------------------------------
    // 결제 취소 - 에러 핸들링
    //-----------------------------------

    @Transactional
    public PaymentCancelPreparedDto validateAndPrepareCancel(PaymentCancelRequest dto) {
        Payment payment = paymentRepository.findByTossOrderIdWithOrder(dto.tossOrderId())
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

        Order order = payment.getOrder();

        List<Delivery> deliveries = deliveryRepository.findAllByOrderId(order.getId());

        for (Delivery delivery : deliveries) {
            if (delivery.getDeliveryStatus() != DeliveryStatus.PREPARING) {
                // 이미 배송 처리가 되었다면 예외 발생
                throw new AppException(PaymentErrorCode.ALREADY_SHIPPED);
            }
            delivery.changeDeliveryStatus(DeliveryStatus.CANCEL_REQUESTED);
        }

        return new PaymentCancelPreparedDto(dto.tossOrderId(), order.getId());
    }

    @Transactional
    public void completeCancelSuccess(PaymentCancelRequest dto, String pgTid, String responseStr) {

        Payment payment = paymentRepository.findByTossOrderIdWithOrder(dto.tossOrderId()).orElseThrow(() -> new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        Order order = payment.getOrder();

        PaymentStatus previousStatus = payment.getPayStatus();

        payment.cancelPayment(dto.cancelAmount());
        order.changeOrderStatus(OrderStatus.CANCELLED);
        order.getDeliveries().forEach(delivery -> delivery.changeDeliveryStatus(DeliveryStatus.CANCELLED));

        publisher.publishEvent(new PaymentStatusChangedEvent(
                payment, previousStatus, dto.cancelReason(),
                PaymentStatus.CANCELLED, pgTid, responseStr ));
    }

    @Transactional
    public void failCancel(String tossOrderId) {
        Payment payment = paymentRepository.findByTossOrderIdWithOrder(tossOrderId).orElseThrow(() -> new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        Order order = payment.getOrder();
        order.getDeliveries().forEach(delivery -> delivery.changeDeliveryStatus(DeliveryStatus.PREPARING));
    }

    @Transactional
    public void handleCancelTimeout(String tossOrderId, int cancelAmount, String cancelReason) {
        Payment payment = paymentRepository.findByTossOrderIdWithOrder(tossOrderId).orElseThrow(() -> new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        Order order = payment.getOrder();

        payment.prepareCancellation(cancelAmount, cancelReason);
        order.changeOrderStatus(OrderStatus.CANCEL_REQUESTED);
        order.getDeliveries().forEach(delivery -> delivery.changeDeliveryStatus(DeliveryStatus.DELIVERY_HOLD));
    }

    @Retry(name = "tossConfirmRetry", fallbackMethod = "checkPaymentStatus")
    public Map<String, Object> requestConfirm(PaymentConfirmRequest dto) {
        return paymentClient.requestConfirm(dto);
    }

    public Map<String, Object> checkPaymentStatus(PaymentConfirmRequest dto, Exception e) {

        if (e instanceof WebClientResponseException responseException) {
            if (responseException.getStatusCode().is4xxClientError()) {
                throw new AppException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
            }
        }
        Map<String, Object> statusMap;

        try {
            statusMap = paymentClient.checkPaymentStatus(dto.paymentKey());
        } catch (Exception ex) {
            throw new AppException(PaymentErrorCode.PAYMENT_NETWORK_TIMEOUT);
        }

        String status = (String) statusMap.get("status");
        if ("DONE".equals(status)) {
            return statusMap;
        }else if ("IN_PROGRESS".equals(status)) {
            log.warn("[결제 보류] 상태가 IN_PROGRESS 입니다. 망취소 로직으로 전환합니다.");
            throw new AppException(PaymentErrorCode.PAYMENT_NETWORK_TIMEOUT);
        }

        throw new AppException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
    }

    public Map<String, Object> requestCanceled(PaymentCancelRequest dto) {
        return paymentClient.requestCanceled(dto);
    }
}
