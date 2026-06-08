package com.example.i_commerce.domain.order.unit.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.verify;

import com.example.i_commerce.domain.order.client.TossPaymentClient;
import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.order.entity.emuns.PaymentStatus;
import com.example.i_commerce.domain.order.event.dto.PaymentStatusChangedEvent;
import com.example.i_commerce.domain.order.exception.PaymentErrorCode;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.domain.order.service.AutoPaymentCancelService;
import com.example.i_commerce.domain.order.service.dto.PaymentCancelRequest;
import com.example.i_commerce.domain.product.facade.StockFacade;
import com.example.i_commerce.global.exception.AppException;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AutoPaymentCancelServiceTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    ApplicationEventPublisher publisher;

    @Mock
    StockFacade stockFacade;

    @Mock
    TossPaymentClient tossPaymentClient;

    @InjectMocks
    AutoPaymentCancelService autoPaymentCancelService;

    private Payment payment;
    private Order order;
    String tossOrderId = "toss_order-id";
    String paymentKey = "toss_payment-key";

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .userId(1L)
                .orderStatus(OrderStatus.PENDING)
                .build();
        payment = Payment.builder()
                .id(1L)
                .amount(10000)
                .pgTid(null)
                .payStatus(PaymentStatus.READY)
                .order(order)
                .build();
    }

    @Test
    @DisplayName("[자동 취소 성공] 토스 취소 API가 성공하면 결제는 FAILED, 주문은 CANCELLED로 변경된다")
    void autoCancelPayment_success() {
        // given
        PaymentCancelRequest dto = new PaymentCancelRequest(tossOrderId, 10000, paymentKey, "재고 부족 자동 취소");
        given(paymentRepository.findByTossOrderIdWithOrder(tossOrderId)).willReturn(Optional.of(payment));
        Map<String, Object> response = Map.of("status", "CANCELED", "paymentKey", "toss_1_123");
        given(tossPaymentClient.requestCanceled(dto)).willReturn(response);

        // when
        autoPaymentCancelService.autoCancelPayment(dto);

        // then
        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);

        verify(publisher, times(1)).publishEvent(any(PaymentStatusChangedEvent.class));
    }

    @Test
    @DisplayName("[자동 취소 타임아웃] 취소 중 타임아웃이 발생하면 주문은 CANCEL_REQUESTED(취소요청)로 격리된다")
    void autoCancelPayment_fail_timeout() {
        // given
        PaymentCancelRequest dto = new PaymentCancelRequest(tossOrderId, 10000, paymentKey, "재고 부족 자동 취소");
        given(paymentRepository.findByTossOrderIdWithOrder(tossOrderId)).willReturn(Optional.of(payment));
        given(tossPaymentClient.requestCanceled(dto))
                .willThrow(new AppException(PaymentErrorCode.PAYMENT_NETWORK_TIMEOUT));

        // when
        autoPaymentCancelService.autoCancelPayment(dto);

        // then
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCEL_REQUESTED);
        verify(publisher, never()).publishEvent(any(PaymentStatusChangedEvent.class));
    }


}