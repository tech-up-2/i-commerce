package com.example.i_commerce.domain.order.client;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.i_commerce.domain.order.exception.PaymentErrorCode;
import com.example.i_commerce.domain.order.service.dto.PaymentCancelRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentConfirmRequest;
import com.example.i_commerce.global.exception.AppException;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class TossPaymentClientTest {

    @MockitoBean
    RestTemplate restTemplate;

    @MockitoSpyBean
    TossPaymentClient tossPaymentClient;

    @Test
    @DisplayName("[결제 성공]결제 승인 시 실패하면 정해진 횟수 만큼 재시도 후 성공한다.")
    public void requestConfirm_Retry_Success() {

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("paymentKey", "PAYMENTKEY");
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        given(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .willThrow(new ResourceAccessException("1차 타임아웃"))
                .willThrow(new ResourceAccessException("2차 타임아웃"))
                .willReturn(response);

        PaymentConfirmRequest dto = new PaymentConfirmRequest("PAYMENT_1", "toss_1_123", 10000);

        tossPaymentClient.requestConfirm(dto);

        verify(restTemplate, times(3)).postForEntity(contains("https://api.tosspayments.com/v1/payments"), any(), eq(Map.class));
    }

    @Test
    @DisplayName("[결제 성공]결제 요청 재시도 모두 실패 후 fallback함수로 결제를 성공한다.")
    public void requestConfirm_Exhausted_CheckPaymentStatus_Success() {

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("paymentKey", "PAYMENTKEY");
        ResponseEntity<Map> response = new ResponseEntity<>(responseBody, HttpStatus.OK);

        given(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .willThrow(new ResourceAccessException("1차 타임아웃"))
                .willThrow(new ResourceAccessException("2차 타임아웃"))
                .willThrow(new ResourceAccessException("3차 타임아웃"));

        given(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(), eq(Map.class)))
                .willReturn(response);

        PaymentConfirmRequest dto = new PaymentConfirmRequest("PAYMENT_1", "toss_1_123", 10000);

        tossPaymentClient.requestConfirm(dto);

        verify(tossPaymentClient).checkPaymentStatus(any(PaymentConfirmRequest.class), any(ResourceAccessException.class));
    }

    @Test
    @DisplayName("[결제 취소] 결제 취소 중 타임아웃이 발생하면 재시도 없이 즉시 1번만 호출되고 타임아웃 에러를 던진다")
    public void requestCanceled_Timeout_ThrowTimeoutInstantly() {
        given(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .willThrow(new ResourceAccessException("취소 API 타임아웃"));

        PaymentCancelRequest dto = new PaymentCancelRequest(1L, 10000, "PAYMENT_KEY_1", "고객 변심");


        assertThatThrownBy(() -> tossPaymentClient.requestCanceled(dto))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.PAYMENT_NETWORK_TIMEOUT);

        verify(restTemplate, times(1)).postForEntity(anyString(), any(), eq(Map.class));
    }

}