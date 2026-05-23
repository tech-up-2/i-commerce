package com.example.i_commerce.domain.order.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.i_commerce.domain.order.client.TossPaymentClient;
import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.order.entity.emuns.PaymentStatus;
import com.example.i_commerce.domain.order.event.dto.PaymentApprovedEvent;
import com.example.i_commerce.domain.order.event.dto.PaymentStatusChangedEvent;
import com.example.i_commerce.domain.order.exception.PaymentErrorCode;
import com.example.i_commerce.domain.order.repository.OrderRepository;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.domain.order.service.dto.PaymentCancelRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentConfirmRequest;
import com.example.i_commerce.domain.product.facade.StockFacade;
import com.example.i_commerce.global.exception.AppException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.ResourceAccessException;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    ApplicationEventPublisher publisher;

    @Mock
    StockFacade stockFacade;

    @Mock
    TossPaymentClient tossPaymentClient;

    @InjectMocks
    PaymentService paymentService;

    private Payment payment;
    private Order order;

    @BeforeEach
    void setUp() {
//        order =
        order = Order.builder()
                .userId(1L)
                .orderStatus(OrderStatus.PENDING)
                .build();
        payment = Payment.builder()
                .id(1L)
                .amount(10000)
                .pgTid("toss_1_123")
                .payStatus(PaymentStatus.READY)
                .order(order)
                .build();
    }

    @Test
    @DisplayName("결제 승인 성공: 상태가 PAID로 변경되고 이벤트가 발행된다")
    void confirmPayment_Success() {
        // given
        PaymentConfirmRequest dto = new PaymentConfirmRequest("PAYMENT_1", "toss_1_123", 10000);
        ReflectionTestUtils.setField(payment, "pgTid", null);
        ReflectionTestUtils.setField(payment, "cancelableAmount", 0);
        given(paymentRepository.findByTossOrderIdWithOrder("toss_1_123")).willReturn(Optional.of(payment));


        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("paymentKey", "toss_1_123");

        given(tossPaymentClient.requestConfirm(any()))
                .willReturn(responseBody);

        // when
        paymentService.confirmPayment(dto);

        // then
        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getCancelableAmount()).isEqualTo(dto.amount());
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);
        verify(publisher).publishEvent(any(PaymentApprovedEvent.class));
    }

    @Test
    @DisplayName("주문 상태가 READY가 아니면 예외가 발생한다.")
    void getPaymentDetails_Fail_invalidPaymentStatus() {
        Long userId = 1L;

        ReflectionTestUtils.setField(order, "orderStatus", OrderStatus.CONFIRMED);

        given(orderRepository.findById(1L)).willReturn(Optional.of(order));

        AppException e = assertThrows(AppException.class, () -> paymentService.getPaymentDetails(userId, 1L));
        Assertions.assertEquals("INVALID_PAYMENT_STATUS", e.getErrorCode().toString());
    }

    @Test
    @DisplayName("결제 승인 실패: DB 금액과 요청 금액이 다르면 예외가 발생한다")
    void confirmPayment_Fail_InvalidAmount() {
        // given
        PaymentConfirmRequest dto = new PaymentConfirmRequest("PAYMENT_1", "toss_1_123", 20000); // 금액 다름
        given(paymentRepository.findByTossOrderIdWithOrder("toss_1_123")).willReturn(Optional.of(payment));


        // when & then
        AppException exception = assertThrows(AppException.class, () -> paymentService.confirmPayment(dto));
        assertThat(exception.getErrorCode()).isEqualTo(PaymentErrorCode.INVALID_PAYMENT_AMOUNT);
    }

    @Test
    @DisplayName("결제 승인 실패: 이미 결제 완료된 상태라면 예외가 발생한다")
    void confirmPayment_Fail_AlreadyPaid() {
        // given
        payment.changePayStatus(PaymentStatus.PAID); // 이미 결제된 상태
        PaymentConfirmRequest dto = new PaymentConfirmRequest("PAYMENT_1", "toss_1_123", 10000);
        given(paymentRepository.findByTossOrderIdWithOrder("toss_1_123")).willReturn(Optional.of(payment));


        // when & then
        AppException exception = assertThrows(AppException.class, () -> paymentService.confirmPayment(dto));
        assertThat(exception.getErrorCode()).isEqualTo(PaymentErrorCode.INVALID_PAYMENT_STATUS);
    }

    @Test
    @DisplayName("결제 승인 실패: 외부 API에러로 인해 실패가 발생한다.")
    void confirmPayment_Fail_FiledTossPayments() {
        PaymentConfirmRequest dto = new PaymentConfirmRequest("PAYMENT_1", "toss_1_123", 10000);
        given(paymentRepository.findByTossOrderIdWithOrder("toss_1_123")).willReturn(Optional.of(payment));
        given(tossPaymentClient.requestConfirm(dto)).willThrow(new AppException(PaymentErrorCode.PAYMENT_NETWORK_TIMEOUT));

        assertThatThrownBy(()-> paymentService.confirmPayment(dto))
                .isInstanceOf(AppException.class);

        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.UNKNOWN_HOLD);
    }

    @Test
    @DisplayName("결제 취소 성공: 상태가 CANCEL로 변경된다")
    void cancelPayment_Success() {
        // given
        PaymentCancelRequest dto = new PaymentCancelRequest(1L, 10000, "toss_1_123", "단순 변심");
        ReflectionTestUtils.setField(payment, "cancelableAmount", 10000);
        ReflectionTestUtils.setField(payment, "payStatus", PaymentStatus.PAID);
        given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("paymentKey", "toss_1_123");

        given(tossPaymentClient.requestCanceled(any()))
                .willReturn(responseBody);

        // when
        paymentService.cancelPayment(dto);

        // then
        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(payment.getCancelableAmount()).isEqualTo(0);
        assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);
        verify(publisher).publishEvent(any(PaymentStatusChangedEvent.class));
    }

    @Test
    @DisplayName("결제 취소 실패: 입력한 paymentKey와 저장된 paymentKey가 다른 경우 예외가 발생한다.")
    void cancelPayment_Fail_InvalidPaymentKey() {
        // given
        PaymentCancelRequest dto = new PaymentCancelRequest(1L, 10000, "toss_1_000", "단순 변심"); // 잘못된 paymentKey
        ReflectionTestUtils.setField(payment, "cancelableAmount", 10000);
        given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

        AppException exception = assertThrows(AppException.class, () -> paymentService.cancelPayment(dto));
        assertThat(exception.getErrorCode()).isEqualTo(PaymentErrorCode.INVALID_PAYMENT_KEY);
    }

    @Test
    @DisplayName("결제 취소 실패: 취소 금액이 취소 가능 금액보다 큰경우 경우 예외가 발생한다.")
    void cancelPayment_Fail_InvalidCancelAmount() {
        // given
        PaymentCancelRequest dto = new PaymentCancelRequest(1L, 99999, "toss_1_123", "단순 변심"); // 잘못된 paymentKey
        ReflectionTestUtils.setField(payment, "cancelableAmount", 10000);
        given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

        AppException exception = assertThrows(AppException.class, () -> paymentService.cancelPayment(dto));
        assertThat(exception.getErrorCode()).isEqualTo(PaymentErrorCode.INVALID_CANCEL_AMOUNT);
    }

    @Test
    @DisplayName("결제 취소 실패: PAID상태가 아닌 경우 결제가 실패하고 예외가 발생한다.")
    void cancelPayment_Fail_InvalidPaymentStatus() {
        // given
        PaymentCancelRequest dto = new PaymentCancelRequest(1L, 10000, "toss_1_123", "단순 변심"); // 잘못된 paymentKey
        ReflectionTestUtils.setField(payment, "cancelableAmount", 10000);
        ReflectionTestUtils.setField(payment, "payStatus", PaymentStatus.CANCELLED);
        given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

        AppException exception = assertThrows(AppException.class, () -> paymentService.cancelPayment(dto));
        assertThat(exception.getErrorCode()).isEqualTo(PaymentErrorCode.PAYMENT_ALREADY_CANCELLED);
    }

    @Test
    @DisplayName("결제 취소 실패: 외부 API에러로 인해 실패가 발생한다.")
    void cancelPayment_Fail_FiledTossPayments() {
        PaymentCancelRequest dto = new PaymentCancelRequest(1L, 10000, "toss_1_123", "단순 변심");

        ReflectionTestUtils.setField(payment, "cancelableAmount", 10000);
        ReflectionTestUtils.setField(payment, "payStatus", PaymentStatus.PAID);
        given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

//        given(paymentRepository.findByTossOrderIdWithOrder("toss_1_123")).willReturn(Optional.of(payment));
        given(tossPaymentClient.requestCanceled(dto)).willThrow(new AppException(PaymentErrorCode.PAYMENT_NETWORK_TIMEOUT));

        assertThatThrownBy(()-> paymentService.cancelPayment(dto))
                .isInstanceOf(AppException.class);

        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.CANCEL_UNKNOWN_HOLD);
    }
}
