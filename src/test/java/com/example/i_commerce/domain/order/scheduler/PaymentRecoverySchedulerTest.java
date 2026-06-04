package com.example.i_commerce.domain.order.scheduler;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.i_commerce.domain.order.client.TossPaymentClient;
import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.order.entity.emuns.PaymentStatus;
import com.example.i_commerce.domain.order.exception.PaymentErrorCode;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.domain.product.facade.StockFacade;
import com.example.i_commerce.global.exception.AppException;
import java.util.HashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PaymentRecoverySchedulerTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    TossPaymentClient tossPaymentClient;

    @Mock
    StockFacade stockFacade;

    @InjectMocks
    PaymentRecoveryScheduler scheduler;

    private Payment payment;
    private Order order;

    @BeforeEach
    void setUp() {
        // 테스트용 연관관계 엔티티 세팅
        order = Order.builder().build();
        ReflectionTestUtils.setField(order, "id", 1L);
        order.changeOrderStatus(OrderStatus.UNKNOWN_HOLD);

        payment = Payment.builder()
                .id(1L)
                .amount(10000)
                .pgTid("toss_key_123")
                .payStatus(PaymentStatus.UNKNOWN_HOLD)
                .order(order)
                .cancelAmount(10000)
                .cancelReason("고객 변심")
                .build();
    }

    @Test
    @DisplayName("승인 홀드 건 복구 성공 - 토스 장부가 DONE이면 결제완료 및 주문확정 상태로 바뀐다")
    void processUnknownHoldPayments_Success() {
        // when
        scheduler.processUnknownHoldPayments(payment, "DONE");

        // then
        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    @Test
    @DisplayName("승인 홀드 건 복구 실패 - 토스 장부에 내역이 없으면 결제실패 및 주문취소 상태로 종결된다")
    void processUnknownHoldPayments_Fail() {
        // when
        scheduler.processUnknownHoldPayments(payment, "NONE");

        // then
        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
    }

    @Test
    @DisplayName("취소 홀드 건 복구 성공 - 토스 장부가 CANCELED면 국내 DB도 취소 완료 처리되고 재고가 복구된다")
    void processCancelRequestedPayments_AlreadyCanceled() {
        // given
        payment.changePayStatus(PaymentStatus.CANCEL_UNKNOWN_HOLD);
        order.changeOrderStatus(OrderStatus.CANCEL_REQUESTED);

        // when
        scheduler.processCancelRequestedPayments(payment, "CANCELED");

        // then
        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);

        // ✨ 핵심: 재고 복구 퍼사드가 정상적으로 호출되었는지 검증
        verify(stockFacade, times(1)).rollbackStocks(order.getId());
    }

    @Test
    @DisplayName("취소 홀드 건 복구 재시도 - 토스 장부가 DONE(취소누락)이면 강제 환불 API를 호출한 뒤 마감한다")
    void processCancelRequestedPayments_RetryCancelSuccess() {
        // given
        payment.changePayStatus(PaymentStatus.CANCEL_UNKNOWN_HOLD);
        order.changeOrderStatus(OrderStatus.CANCEL_REQUESTED);

        // 토스 취소 API가 정상 성공할 것이라고 모킹
        given(tossPaymentClient.requestCanceled(any())).willReturn(new HashMap<>());

        // when
        scheduler.processCancelRequestedPayments(payment, "DONE");

        // then
        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);

        // ✨ 핵심: 토스 취소 API와 재고 복구가 순차적으로 강제 실행되었는지 검증
        verify(tossPaymentClient, times(1)).requestCanceled(any());
        verify(stockFacade, times(1)).rollbackStocks(order.getId());
    }

    @Test
    @DisplayName("취소 홀드 건 복구 재시도 - 토스 장부가 DONE(취소누락)일 때 강제 환불 API가 잘못된 요청으로 실패하면 주문과 결제를 결제된 상태로 변경한 뒤 마감한다")
    void processCancelRequestedPayments_RetryCancelFail() {
        // given
        payment.changePayStatus(PaymentStatus.CANCEL_UNKNOWN_HOLD);
        order.changeOrderStatus(OrderStatus.CANCEL_REQUESTED);

        // 토스 취소 API가 정상 성공할 것이라고 모킹
        given(tossPaymentClient.requestCanceled(any())).willThrow(new AppException(
                PaymentErrorCode.PAYMENT_CANCEL_FAILED));

        // when
        scheduler.processCancelRequestedPayments(payment, "DONE");

        // then
        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);

    }
}