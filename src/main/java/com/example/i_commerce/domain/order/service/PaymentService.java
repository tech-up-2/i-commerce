package com.example.i_commerce.domain.order.service;

import com.example.i_commerce.domain.order.client.TossPaymentClient;
import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.order.entity.emuns.PaymentStatus;
import com.example.i_commerce.domain.order.event.dto.DeliveryCancelRequestEvent;
import com.example.i_commerce.domain.order.event.dto.PaymentApprovedEvent;
import com.example.i_commerce.domain.order.event.dto.PaymentStatusChangedEvent;
import com.example.i_commerce.domain.order.exception.PaymentErrorCode;
import com.example.i_commerce.domain.order.repository.OrderRepository;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.domain.order.service.dto.PaymentCancelRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentConfirmRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentDetailResponse;
import com.example.i_commerce.domain.product.facade.StockFacade;
import com.example.i_commerce.domain.product.facade.dto.StockDeductCommand;
import com.example.i_commerce.global.exception.AppException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher publisher;
    private final StockFacade stockFacade;
    private final TossPaymentClient tossPaymentClient;


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
                .filter(payment -> payment.getPayStatus() == PaymentStatus.READY)
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

    @Transactional
    public void confirmPayment(PaymentConfirmRequest dto) {
        Payment payment = paymentRepository.findByTossOrderIdWithOrder(dto.tossOrderId())
                .orElseThrow(() -> new AppException(PaymentErrorCode.PAYMENT_NOT_FOUND));

        if(payment.getPayStatus() != PaymentStatus.READY) {
            throw new AppException(PaymentErrorCode.INVALID_PAYMENT_STATUS);
        }

        if (!payment.getAmount().equals(dto.amount())) {
            throw new AppException(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
        }
        Order order = payment.getOrder();
        List<StockDeductCommand> stockDeductCommands = order.getOrderProducts().stream()
                .map(orderProduct ->
                        new StockDeductCommand(
                                orderProduct.getProductSkuId(),
                                orderProduct.getCount(),
                                order.getId()
                        ))
                .toList();

        stockFacade.deductStock(stockDeductCommands);

        try {
            Map<String, Object> response = tossPaymentClient.requestConfirm(dto);

            PaymentStatus previousStatus = payment.getPayStatus();
            String pgTid = (String) response.get("paymentKey");

            payment.completePayment(pgTid);
            payment.getOrder().changeOrderStatus(OrderStatus.CONFIRMED);

            publisher.publishEvent(new PaymentApprovedEvent(payment.getOrder().getId()));

            publisher.publishEvent(new PaymentStatusChangedEvent(
                    payment,
                    previousStatus,
                    "결제 완료",
                    PaymentStatus.PAID,
                    pgTid,
                    response.toString()));

        } catch (AppException e) {

            if(e.getErrorCode() == PaymentErrorCode.PAYMENT_NOT_FOUND ||
                    e.getErrorCode() == PaymentErrorCode.PAYMENT_CONFIRM_FAILED) {
                payment.changePayStatus(PaymentStatus.FAILED);
                throw new AppException(PaymentErrorCode.PAYMENT_CONFIRM_FAILED);
            }

            if (e.getErrorCode() == PaymentErrorCode.PAYMENT_NETWORK_TIMEOUT) {
                payment.changePayStatus(PaymentStatus.UNKNOWN_HOLD);
                order.changeOrderStatus(OrderStatus.UNKNOWN_HOLD);
                throw new AppException(PaymentErrorCode.PAYMENT_UNKNOWN_HOLD);
            }
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

        Map<String, Object> response;
        try {
            response = tossPaymentClient.requestCanceled(dto);

        } catch (AppException e) {
            if (e.getErrorCode() == PaymentErrorCode.PAYMENT_NETWORK_TIMEOUT) {
                log.error("💥 [취소 비상] 결제 취소 중 타임아웃 발생. 토스 장부 확인 불가.");

                payment.changePayStatus(PaymentStatus.CANCEL_UNKNOWN_HOLD);
                order.changeOrderStatus(OrderStatus.CANCEL_REQUESTED);

                throw new AppException(PaymentErrorCode.PAYMENT_UNKNOWN_HOLD);
            }
            throw e;
        }

        PaymentStatus previousStatus = payment.getPayStatus();
        String pgTid = (String) response.get("paymentKey");

        payment.cancelPayment(dto.cancelAmount());
        order.changeOrderStatus(OrderStatus.CANCELLED);

        stockFacade.rollbackStocks(order.getId());

        publisher.publishEvent(new PaymentStatusChangedEvent(
                payment,
                previousStatus,
                dto.cancelReason(),
                PaymentStatus.CANCELLED,
                pgTid,
                response.toString()));
    }
}
