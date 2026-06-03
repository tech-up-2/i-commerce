package com.example.i_commerce.domain.order.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.i_commerce.domain.order.client.PaymentClient;
import com.example.i_commerce.domain.order.client.TossPaymentClient;
import com.example.i_commerce.domain.order.exception.PaymentErrorCode;
import com.example.i_commerce.domain.order.repository.DeliveryRepository;
import com.example.i_commerce.domain.order.repository.OrderRepository;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.domain.order.service.dto.PaymentConfirmRequest;
import com.example.i_commerce.global.exception.AppException;
import io.github.resilience4j.springboot3.retry.autoconfigure.RetryAutoConfiguration;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@ExtendWith(SpringExtension.class)
@Import(RetryAutoConfiguration.class)
@EnableAspectJAutoProxy(proxyTargetClass = true)
@TestPropertySource(properties = {
        "resilience4j.retry.instances.tossConfirmRetry.maxAttempts=3",
        "resilience4j.retry.instances.tossConfirmRetry.ignoreExceptions[0]=org.springframework.web.reactive.function.client.WebClientResponseException$BadRequest",
        "resilience4j.retry.instances.tossConfirmRetry.ignoreExceptions[1]=org.springframework.web.reactive.function.client.WebClientResponseException$Unauthorized",
        "resilience4j.retry.instances.tossConfirmRetry.ignoreExceptions[2]=org.springframework.web.reactive.function.client.WebClientResponseException$Forbidden",
        "resilience4j.retry.instances.tossConfirmRetry.ignoreExceptions[3]=org.springframework.web.reactive.function.client.WebClientResponseException$NotFound"
})
@SpringJUnitConfig(classes = { PaymentService.class, TossPaymentClient.class })
public class PaymentServiceRetryTest {

    @MockitoSpyBean
    private PaymentService paymentService;

    @MockitoBean private PaymentClient paymentClient;
    @MockitoBean private PaymentRepository paymentRepository;
    @MockitoBean private OrderRepository orderRepository;
    @MockitoBean private DeliveryRepository deliveryRepository;
    @MockitoBean private ApplicationEventPublisher publisher;

    private PaymentConfirmRequest paymentConfirmRequest;

    @BeforeEach
    void setUp() {
        paymentConfirmRequest = new PaymentConfirmRequest("PAYMENT_1", "toss_1_123", 10000);
    }

    @Test
    @DisplayName("[결제 성공]결제 승인 시 실패하면 정해진 횟수 만큼 재시도 후 성공한다.")
    void requestConfirm_Retry_Success() {
        doThrow(WebClientResponseException.create(500, "Internal Server Error", null, null, null))
                .doThrow(WebClientResponseException.create(500, "Internal Server Error", null, null, null))
                .doReturn(Map.of("paymentKey", "PAYMENTKEY"))
                .when(paymentClient).requestConfirm(any(PaymentConfirmRequest.class));
        Map<String, Object> result = paymentService.requestConfirm(paymentConfirmRequest);

        assertThat(result).containsEntry("paymentKey", "PAYMENTKEY");
        verify(paymentClient, times(3)).requestConfirm(any(PaymentConfirmRequest.class));
    }

    @Test
    @DisplayName("[결제 성공] 3번 타임 아웃 실패 후 폴백 함수에서 토스 조회가 DONE 이면 최종 성공한다.")
    void requestConfirm_Exhausted_Fallback_Done_Success() {

        doThrow(WebClientResponseException.create(500, "Internal Server Error", null, null, null))
                .doThrow(WebClientResponseException.create(500, "Internal Server Error", null, null, null))
                .doThrow(WebClientResponseException.create(500, "Internal Server Error", null, null, null))
                .when(paymentClient).requestConfirm(any(PaymentConfirmRequest.class));

        when(paymentClient.checkPaymentStatus(any(String.class)))
                .thenReturn(Map.of("status", "DONE"));

        Map<String, Object> result = paymentService.requestConfirm(paymentConfirmRequest);

        assertThat(result).isEqualTo(Map.of("status", "DONE"));
        verify(paymentService).checkPaymentStatus(any(PaymentConfirmRequest.class), any(Exception.class));
    }

    @Test
    @DisplayName("[결제 실패] 사용자의 잘못으로 400번대 실패 후 폴백 함수에서 결제 실패로 처리한다.")
    void requestConfirm_4xx_Fallback_Fail() {

        doThrow(WebClientResponseException.create(400, "Bad Request", null, null, null))
                .when(paymentClient).requestConfirm(any(PaymentConfirmRequest.class));


        assertThatThrownBy(() -> paymentService.requestConfirm(paymentConfirmRequest))
                .isInstanceOf(AppException.class)
                        .hasMessage(PaymentErrorCode.PAYMENT_CONFIRM_FAILED.getMessage());


        verify(paymentService, times(1)).requestConfirm(any(PaymentConfirmRequest.class));
        verify(paymentService).checkPaymentStatus(any(PaymentConfirmRequest.class), any(Exception.class));
    }

    @Test
    @DisplayName("[결제 실패] 3번 타임 아웃 실패 후 폴백 함수에서 토스 조회가 IN_PROGRESS이면 타임아웃이 발생한다.")
    void requestConfirm_Exhausted_Fallback_INPROGRESS_Fail() {

        doThrow(WebClientResponseException.create(500, "Internal Server Error", null, null, null))
                .doThrow(WebClientResponseException.create(500, "Internal Server Error", null, null, null))
                .doThrow(WebClientResponseException.create(500, "Internal Server Error", null, null, null))
                .when(paymentClient).requestConfirm(any(PaymentConfirmRequest.class));

        when(paymentClient.checkPaymentStatus(any(String.class)))
                .thenReturn(Map.of("status", "IN_PROGRESS"));

        assertThatThrownBy(() -> paymentService.requestConfirm(paymentConfirmRequest))
                .isInstanceOf(AppException.class)
                .hasMessage(PaymentErrorCode.PAYMENT_NETWORK_TIMEOUT.getMessage());

        verify(paymentService, times(3)).requestConfirm(any(PaymentConfirmRequest.class));
        verify(paymentService).checkPaymentStatus(any(PaymentConfirmRequest.class), any(Exception.class));
    }

    @Test
    @DisplayName("[결제 실패] 3번 타임 아웃 실패 후 폴백 함수에서 토스 조회가 타임아웃이면 타임아웃이 발생한다.")
    void requestConfirm_Exhausted_Fallback_TimeOut_Fail() {

        doThrow(WebClientResponseException.create(500, "Internal Server Error", null, null, null))
                .doThrow(WebClientResponseException.create(500, "Internal Server Error", null, null, null))
                .doThrow(WebClientResponseException.create(500, "Internal Server Error", null, null, null))
                .when(paymentClient).requestConfirm(any(PaymentConfirmRequest.class));

        when(paymentClient.checkPaymentStatus(any(String.class)))
                .thenReturn(Map.of("status", "IN_PROGRESS"));

        doThrow(WebClientResponseException.create(500, "Internal Server Error", null, null, null))
                .when(paymentClient).checkPaymentStatus(any(String.class));

        assertThatThrownBy(() -> paymentService.requestConfirm(paymentConfirmRequest))
                .isInstanceOf(AppException.class)
                .hasMessage(PaymentErrorCode.PAYMENT_NETWORK_TIMEOUT.getMessage());

        verify(paymentService, times(3)).requestConfirm(any(PaymentConfirmRequest.class));
        verify(paymentService).checkPaymentStatus(any(PaymentConfirmRequest.class), any(Exception.class));
    }

    @Test
    @DisplayName("[결제 실패] 3번 타임 아웃 실패 후 폴백 함수에서 토스 조회가 ABORT이면 결제 실패로 처리한다.")
    void requestConfirm_Exhausted_Fallback_ABORT_Fail() {

        doThrow(WebClientResponseException.create(500, "Internal Server Error", null, null, null))
                .doThrow(WebClientResponseException.create(500, "Internal Server Error", null, null, null))
                .doThrow(WebClientResponseException.create(500, "Internal Server Error", null, null, null))
                .when(paymentClient).requestConfirm(any(PaymentConfirmRequest.class));

        when(paymentClient.checkPaymentStatus(any(String.class)))
                .thenReturn(Map.of("status", "ABORT"));

        assertThatThrownBy(() -> paymentService.requestConfirm(paymentConfirmRequest))
                .isInstanceOf(AppException.class)
                .hasMessage(PaymentErrorCode.PAYMENT_CONFIRM_FAILED.getMessage());

        verify(paymentService, times(3)).requestConfirm(any(PaymentConfirmRequest.class));
        verify(paymentService).checkPaymentStatus(any(PaymentConfirmRequest.class), any(Exception.class));
    }
}
