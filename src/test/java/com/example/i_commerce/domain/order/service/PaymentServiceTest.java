package com.example.i_commerce.domain.order.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.order.entity.emuns.PaymentStatus;
import com.example.i_commerce.domain.order.event.dto.PaymentCompletedEvent;
import com.example.i_commerce.domain.order.exception.PaymentErrorCode;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.domain.order.service.dto.PaymentConfirmRequest;
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
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    RestTemplate restTemplate;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    PaymentService paymentService;

    private Payment payment;
    private Order order;

    @BeforeEach
    void setUp() {
        order = Mockito.mock(Order.class);
        payment = Payment.builder()
                .id(1L)
                .amount(10000)
                .payStatus(PaymentStatus.READY)
                .order(order)
                .build();
    }

    @Test
    @DisplayName("결제 상태가 READY가 아니면 예외가 발생한다.")
    void getPaymentDetails_fail_invalidPaymentStatus() {
        Payment payment = Payment.builder().payStatus(PaymentStatus.PAID).build();
        given(paymentRepository.findById(anyLong())).willReturn(Optional.of(payment));

        AppException e = assertThrows(AppException.class, () -> paymentService.getPaymentDetails(1L));
        Assertions.assertEquals("INVALID_PAYMENT_STATUS", e.getErrorCode().toString());
    }

    @Test
    @DisplayName("결제 승인 성공: 상태가 PAID로 변경되고 이벤트가 발행된다")
    void confirmPayment_Success() {
        // given
        PaymentConfirmRequest dto = new PaymentConfirmRequest("PAYMENT_1", "toss_1_123", 10000);
        given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("paymentKey", "toss_1_123");
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(responseBody, HttpStatus.OK);

        given(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .willReturn(responseEntity);

        // when
        paymentService.confirmPayment(dto);

        // then
        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.PAID);
        verify(order).changeOrderStatus(OrderStatus.CONFIRMED);
        verify(publisher).publishEvent(any(PaymentCompletedEvent.class));
    }

    @Test
    @DisplayName("결제 승인 실패: DB 금액과 요청 금액이 다르면 예외가 발생한다")
    void confirmPayment_Fail_InvalidAmount() {
        // given
        PaymentConfirmRequest dto = new PaymentConfirmRequest("PAYMENT_1", "toss_1_123", 20000); // 금액 다름
        given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

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
        given(paymentRepository.findById(1L)).willReturn(Optional.of(payment));

        // when & then
        AppException exception = assertThrows(AppException.class, () -> paymentService.confirmPayment(dto));
        assertThat(exception.getErrorCode()).isEqualTo(PaymentErrorCode.INVALID_PAYMENT_STATUS);
    }

    //TODO : 외부 API가 실패 했을 경우 테스트 추가하기 (RestTemplate이 예외를 던지는 경우)

}
