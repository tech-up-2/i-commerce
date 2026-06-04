package com.example.i_commerce.domain.order.scheduler;

import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.emuns.DeliveryStatus;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.order.entity.emuns.PaymentStatus;
import com.example.i_commerce.domain.order.exception.PaymentErrorCode;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.domain.order.client.TossPaymentClient;
import com.example.i_commerce.domain.order.service.dto.PaymentCancelRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentConfirmRequest;
import com.example.i_commerce.domain.product.facade.StockFacade;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentRecoveryScheduler {

    private final PaymentRepository paymentRepository;
    private final TossPaymentClient tossPaymentClient;
    private final StockFacade stockFacade;

    @Scheduled(fixedDelay = 300000)
    public void recoverUnresolvedPayments() {
        recoverUnknownHoldPayments();
        recoverCancelRequestedPayments();
    }

    private void recoverUnknownHoldPayments() {
        List<Payment> paymentList = paymentRepository.findAllByPayStatus(PaymentStatus.UNKNOWN_HOLD);

        for(Payment payment : paymentList) {
            try{
                Map<String, Object> response = tossPaymentClient.checkPaymentStatus(payment.getPgTid());
                String status = (String) response.get("status");

                processUnknownHoldPayments(payment, status);
            } catch (Exception e) {
                log.error("[취소 복구 실패] 주문 ID: {} 처리 중 외부 API 오류 발생. 다음 주기에 재시도합니다.", payment.getOrder().getId(), e);
            }

        }
    }

    private void recoverCancelRequestedPayments() {
        List<Payment> paymentList = paymentRepository.findAllByPayStatus(PaymentStatus.CANCEL_UNKNOWN_HOLD);

        for(Payment payment : paymentList) {
            try{
                Map<String, Object> response = tossPaymentClient.checkPaymentStatus(payment.getPgTid());
                String status = (String) response.get("status");

                processCancelRequestedPayments(payment,status);
            } catch (Exception e) {
                log.error("[취소 복구 실패] 주문 ID: {} 처리 중 외부 API 오류 발생. 다음 주기에 재시도합니다.", payment.getOrder().getId(), e);
            }

        }
    }

    @Transactional
    public void processUnknownHoldPayments(Payment payment, String status) {
        Order order = payment.getOrder();

        if("DONE".equals(status)) {
            payment.changePayStatus(PaymentStatus.PAID);
            order.changeOrderStatus(OrderStatus.CONFIRMED);
        } else {
            payment.changePayStatus(PaymentStatus.FAILED);
            order.changeOrderStatus(OrderStatus.CANCELLED);
        }
    }

    @Transactional
    public void processCancelRequestedPayments(Payment payment, String status) {
        Order order = payment.getOrder();

        if("CANCELED".equals(status)) {
            payment.changePayStatus(PaymentStatus.CANCELLED);
            order.changeOrderStatus(OrderStatus.CANCELLED);
            stockFacade.rollbackStocks(order.getId());
        } else if("DONE".equals(status)){
            PaymentCancelRequest dto = new PaymentCancelRequest(
                    payment.getTossOrderId(),
                    payment.getCancelAmount(),
                    payment.getPgTid(),
                    payment.getCancelReason()
            );
            try {
                // TODO: 트랜잭션 분리
                tossPaymentClient.requestCanceled(dto);
                payment.changePayStatus(PaymentStatus.CANCELLED);
                order.changeOrderStatus(OrderStatus.CANCELLED);
                stockFacade.rollbackStocks(order.getId());
            } catch (AppException e) {
                if (e.getErrorCode() == PaymentErrorCode.PAYMENT_CANCEL_FAILED) {
                    log.error("[취소 재시도 거절] 토스에서 취소 요청을 거절했습니다. 원래 주문 상태로 원복합니다. 주문 ID: {}", order.getId());

                    payment.changePayStatus(PaymentStatus.PAID);
                    order.changeOrderStatus(OrderStatus.CONFIRMED);
                    order.getDeliveries().forEach(d -> d.changeDeliveryStatus(DeliveryStatus.PREPARING));
                }

                if (e.getErrorCode() == PaymentErrorCode.PAYMENT_NETWORK_TIMEOUT) {
                    log.error("[취소 재시도 타임아웃] 토스 API 서버가 여전히 먹통입니다. 다음 주기를 기약합니다. 주문 ID: {}", order.getId());
                }


            }
        }
    }
}
