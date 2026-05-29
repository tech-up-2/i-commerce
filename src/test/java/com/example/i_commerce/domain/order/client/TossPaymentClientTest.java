package com.example.i_commerce.domain.order.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import com.example.i_commerce.domain.order.exception.PaymentErrorCode;
import com.example.i_commerce.domain.order.service.dto.PaymentCancelRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentConfirmRequest;
import com.example.i_commerce.global.exception.AppException;
import io.github.resilience4j.springboot3.retry.autoconfigure.RetryAutoConfiguration;
import java.io.IOException;
import java.util.Map;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okhttp3.mockwebserver.SocketPolicy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.i_commerce.domain.order.config.TestWebClientConfig;

@SpringJUnitConfig(classes = { TossPaymentClient.class, TossPaymentClientTest.TestConfig.class })
@Import(RetryAutoConfiguration.class)
@EnableAspectJAutoProxy
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class TossPaymentClientTest {

    // 각 테스트마다 새 MockWebServer를 생성/종료합니다.
    private MockWebServer mockWebServer;

    @MockitoSpyBean
    TossPaymentClient tossPaymentClient;

    @BeforeEach
    void setUp() throws IOException {
        // 매 테스트마다 새 MockWebServer를 실행하고, 해당 URL을 바라보는 WebClient를 주입합니다.
        mockWebServer = new MockWebServer();
        mockWebServer.start();

        WebClient targetWebClient = TestWebClientConfig.createTestWebClient(mockWebServer.url("/").toString());
        ReflectionTestUtils.setField(tossPaymentClient, "tossWebClient", targetWebClient);
    }

    @AfterEach
    void tearDown() throws IOException {
        // 💡 테스트 종료 즉시 이번 포트의 가상 서버를 확실하게 닫아줍니다.
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    // 초기 스프링 컨텍스트 로딩 시 에러를 막기 위한 최소한의 가짜 가짜(Dummy) 빈 설정
    static class TestConfig {
        @Bean
        public WebClient tossWebClient() {
            return WebClient.builder().build();
        }
    }


    @Test
    @DisplayName("[결제 성공]결제 승인 시 실패하면 정해진 횟수 만큼 재시도 후 성공한다.")
    public void requestConfirm_Retry_Success() throws InterruptedException {
        // given: 1차(500 서버에러), 2차(500 서버에러) -> Resilience4j Retry 트리거
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"paymentKey\": \"PAYMENTKEY\"}"));

        PaymentConfirmRequest dto = new PaymentConfirmRequest("PAYMENT_1", "toss_1_123", 10000);

        // when
        Map<String, Object> result = tossPaymentClient.requestConfirm(dto);

        // then
        assertThat(result).containsEntry("paymentKey", "PAYMENTKEY");

        // 총 3번의 HTTP 요청이 들어왔는지 확인
        assertEquals(3, mockWebServer.getRequestCount());

        // 첫 번째 요청의 스펙 및 경로 검증
        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/confirm", request.getPath());
        assertEquals("POST", request.getMethod());
        assertThat(request.getHeader("Authorization")).startsWith("Basic ");
    }

    @Test
    @DisplayName("[결제 성공]결제 요청 재시도 모두 실패 후 fallback함수로 결제를 성공한다.")
    public void requestConfirm_Exhausted_CheckPaymentStatus_Success() throws InterruptedException {
        // given: 3번의 요청 모두 500 에러 발생하여 재시도 고갈시킴
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));
        mockWebServer.enqueue(new MockResponse().setResponseCode(500));

        // 4번째 응답: fallbackMethod인 checkPaymentStatus에서 수행할 단건 조회 성공 데이터
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody("{\"paymentKey\": \"PAYMENTKEY\", \"status\": \"DONE\"}"));

        PaymentConfirmRequest dto = new PaymentConfirmRequest("PAYMENT_1", "toss_1_123", 10000);

        // when
        Map<String, Object> result = tossPaymentClient.requestConfirm(dto);

        // then
        assertThat(result).containsEntry("paymentKey", "PAYMENTKEY");

        // SpyBean으로 fallback 메서드가 실제로 호출되었는지 검증
        verify(tossPaymentClient).checkPaymentStatus(any(PaymentConfirmRequest.class), any(Exception.class));

        // 총 4번의 요청이 가상 서버로 전달되었는지 검증 (재시도 3번 + fallback GET 조회 1번)
        assertEquals(4, mockWebServer.getRequestCount());

        // 4번째 요청은 단건 조회 API 경로("/PAYMENT_1")여야 하며 GET 메서드여야 함
        mockWebServer.takeRequest(); // 1차 패스
        mockWebServer.takeRequest(); // 2차 패스
        mockWebServer.takeRequest(); // 3차 패스
        RecordedRequest fallbackRequest = mockWebServer.takeRequest();

        assertEquals("/PAYMENT_1", fallbackRequest.getPath());
        assertEquals("GET", fallbackRequest.getMethod());
    }

    @Test
    @DisplayName("[결제 취소] 결제 취소 중 타임아웃이 발생하면 재시도 없이 즉시 1번만 호출되고 타임아웃 에러를 던진다")
    public void requestCanceled_Timeout_ThrowTimeoutInstantly() throws InterruptedException {
        // given: WebClientRequestException을 유발하기 위해 연결 끊기(Disconnect) 소켓 정책 설정
        mockWebServer.enqueue(new MockResponse().setSocketPolicy(SocketPolicy.DISCONNECT_AFTER_REQUEST));

        PaymentCancelRequest dto = new PaymentCancelRequest("toss_1_123", 10000, "PAYMENT_KEY_1", "고객 변심");

        // when & then
        assertThatThrownBy(() -> tossPaymentClient.requestCanceled(dto))
                .isInstanceOf(AppException.class)
                .hasFieldOrPropertyWithValue("errorCode", PaymentErrorCode.PAYMENT_NETWORK_TIMEOUT);

        // 가상 서버에 요청이 1번만 들어왔는지 검증
        assertEquals(1, mockWebServer.getRequestCount());

        RecordedRequest request = mockWebServer.takeRequest();
        assertEquals("/PAYMENT_KEY_1/cancel", request.getPath());
    }
}