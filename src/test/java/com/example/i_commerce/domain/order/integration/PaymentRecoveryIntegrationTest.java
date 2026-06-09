package com.example.i_commerce.domain.order.integration;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.i_commerce.common.OrderIntegrationTestSupport;
import com.example.i_commerce.domain.order.client.PaymentClient;
import com.example.i_commerce.domain.order.config.TestWebClientConfig;
import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.order.entity.emuns.PaymentStatus;
import com.example.i_commerce.domain.order.exception.PaymentErrorCode;
import com.example.i_commerce.domain.order.scheduler.PaymentRecoveryScheduler;
import com.example.i_commerce.domain.order.service.dto.CreateOrderRequest;
import com.example.i_commerce.domain.order.service.dto.CreateOrderRequest.OrderItemDto;
import com.example.i_commerce.domain.order.service.dto.PaymentCancelRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentConfirmRequest;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.reactive.function.client.WebClient;

@Slf4j
@SpringBootTest
public class PaymentRecoveryIntegrationTest extends OrderIntegrationTestSupport {


    @Autowired private PaymentClient tossPaymentClient;
    @Autowired private PaymentRecoveryScheduler paymentRecoveryScheduler;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static MockWebServer mockWebServer;

    @BeforeAll
    static void startServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(0);
    }

    @AfterAll
    static void stopServer() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @BeforeEach
    void setUp() {
        mockWebServer.setDispatcher(new okhttp3.mockwebserver.QueueDispatcher());
        WebClient targetWebClient = TestWebClientConfig.createTestWebClient(mockWebServer.url("/").toString());
        ReflectionTestUtils.setField(tossPaymentClient, "tossWebClient", targetWebClient);
    }


    // ==========================================
    // 결제 승인 중 외부 API 타임아웃 발생 (UNKNOWN_HOLD 전환)
    // ==========================================
    @Test
    @DisplayName("[시나리오] 결제 승인 시 3회 타임아웃 실패 후 폴백 함수에서도 토스 조회가 타임아웃이면 UNKNOWN_HOLD 상태로 전환되고 재고는 차감 유지된다.")
    public void requestConfirm_Exhausted_Fallback_TimeOut_Fail_Integration() throws Exception {
        // given
        CustomUserPrincipal testPrincipal = loginAsMember();
        CommerceTestSet commerceTestSet = saveDefaultCommerceTestSet(testPrincipal.getId());

        int quantity1 = 2;
        int quantity2 = 1;
        int expectedStock1 = commerceTestSet.stocks().getFirst().getQuantity() - quantity1;
        int expectedStock2 = commerceTestSet.stocks().get(1).getQuantity() - quantity2;
        String paymentKey = "mock_payment_key";

        enqueueMockResponses(500, "Internal Server Error", 4);
        String tossOrderId = executeOrderAndConfirmSequence(testPrincipal, commerceTestSet, paymentKey, 2, 1);

        Payment payment = paymentRepository.findByTossOrderIdWithOrder(tossOrderId).orElseThrow();

        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.UNKNOWN_HOLD);
        assertThat(payment.getOrder().getOrderStatus()).isEqualTo(OrderStatus.UNKNOWN_HOLD);

        long stockId1 = commerceTestSet.stocks().getFirst().getId();
        long stockId2 = commerceTestSet.stocks().get(1).getId();

        assertThat(stockRepository.findById(stockId1).orElseThrow().getQuantity()).isEqualTo(expectedStock1);
        assertThat(stockRepository.findById(stockId2).orElseThrow().getQuantity()).isEqualTo(expectedStock2);
    }

    // ==========================================
    // 복구 스케줄러 작동 (UNKNOWN_HOLD -> 결제 완료 처리)
    // ==========================================
    @Test
    @DisplayName("[시나리오] 복구 스케줄러 가동 시 UNKNOWN_HOLD 건을 조회하여 토스 측 결제 내역(DONE)이 확인되면 주문 성공으로 변경된다.")
    void scheduler_Recovery_UnknownHold_To_Success_Integration() throws Exception {
        // given
        CustomUserPrincipal testPrincipal = loginAsMember();
        CommerceTestSet commerceTestSet = saveDefaultCommerceTestSet(testPrincipal.getId());

        int quantity1 = 2;
        int quantity2 = 1;
        int expectedStock1 = commerceTestSet.stocks().getFirst().getQuantity() - quantity1;
        int expectedStock2 = commerceTestSet.stocks().get(1).getQuantity() - quantity2;
        String paymentKey = "mock_payment_key";

        enqueueMockResponses(500, "Internal Server Error", 4);
        String tossOrderId = executeOrderAndConfirmSequence(testPrincipal, commerceTestSet, paymentKey,quantity1, quantity2);

        Payment payment = paymentRepository.findByTossOrderIdWithOrder(tossOrderId).orElseThrow();

        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.UNKNOWN_HOLD);
        assertThat(payment.getOrder().getOrderStatus()).isEqualTo(OrderStatus.UNKNOWN_HOLD);

        enqueueMockResponses(200, "{\"status\": \"DONE\", \"paymentKey\": \"mock_payment_key\"}", 1);

        // when
        // 배치 스케줄러 메서드를 강제로 수동 가동
        paymentRecoveryScheduler.recoverUnknownHoldPayments();

        // then: 데이터베이스 최종 상태 검증
        // 1. 주문 상태가 최종적으로 'SUCCESS(결제완료)'로 변경되었는가?
        payment = paymentRepository.findByTossOrderIdWithOrder(tossOrderId).orElseThrow();
        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getOrder().getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);

        long stockId1 = commerceTestSet.stocks().getFirst().getId();
        long stockId2 = commerceTestSet.stocks().get(1).getId();

        assertThat(stockRepository.findById(stockId1).orElseThrow().getQuantity()).isEqualTo(expectedStock1);
        assertThat(stockRepository.findById(stockId2).orElseThrow().getQuantity()).isEqualTo(expectedStock2);
    }

    // ==========================================
    // 복구 스케줄러 작동 (UNKNOWN_HOLD -> 결제 실패 및 재고 원복)
    // ==========================================
    @Test
    @DisplayName("[시나리오] 복구 스케줄러 가동 시 UNKNOWN_HOLD 건의 토스 결제 내역이 없다면 주문 실패 처리 후 재고를 원복한다.")
    void scheduler_Recovery_UnknownHold_To_Fail_And_RollbackStock_Integration() throws Exception {
        // given
        CustomUserPrincipal testPrincipal = loginAsMember();
        CommerceTestSet commerceTestSet = saveDefaultCommerceTestSet(testPrincipal.getId());


        int quantity1 = 2;
        int quantity2 = 1;
        int expectedStock1 = commerceTestSet.stocks().getFirst().getQuantity();
        int expectedStock2 = commerceTestSet.stocks().get(1).getQuantity();
        String paymentKey = "mock_payment_key";

        enqueueMockResponses(500, "Internal Server Error", 4);
        String tossOrderId = executeOrderAndConfirmSequence(testPrincipal, commerceTestSet, paymentKey,quantity1, quantity2);

        Payment payment = paymentRepository.findByTossOrderIdWithOrder(tossOrderId).orElseThrow();

        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.UNKNOWN_HOLD);
        assertThat(payment.getOrder().getOrderStatus()).isEqualTo(OrderStatus.UNKNOWN_HOLD);

        // 토스 조회 결과 내역이 없음(404 NotFound 등)을 리턴한다고 가정
        enqueueMockResponses(404, "Not Found", 1);
        // when
        paymentRecoveryScheduler.recoverUnknownHoldPayments();

        // then: 데이터베이스 최종 상태 검증
        // 1. 주문 상태가 최종적으로 'FAIL(결제실패)'로 확정 변경되었는가?
//        Payment
                payment = paymentRepository.findByTossOrderIdWithOrder(tossOrderId).orElseThrow();
        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.FAILED);

        // 2. ⚡️핵심: 결제가 안 된 것이 밝혀졌으므로 묶여있던 재고가 다시 원복(10개) 되었는가?
        long stockId1 = commerceTestSet.stocks().getFirst().getId();
        long stockId2 = commerceTestSet.stocks().get(1).getId();
        assertThat(stockRepository.findById(stockId1).orElseThrow().getQuantity()).isEqualTo(expectedStock1);
        assertThat(stockRepository.findById(stockId2).orElseThrow().getQuantity()).isEqualTo(expectedStock2);
    }


    @Test
    @DisplayName("[시나리오] 결제 취소 요청시 타임아웃 실패로 토스 CANCEL_UNKNOWN_HOLD 상태로 전환된다.")
    void requestCancel_Timeout_Fail_To_CancelUnknownHold_Integration() throws Exception {
        // given: 주문 생성 및 결제 승인 완료 상태에서 취소 요청 시 토스 API가 타임아웃 발생하여 CANCEL_UNKNOWN_HOLD 상태로 전환되는 시나리오
        CustomUserPrincipal testPrincipal = loginAsMember();
        CommerceTestSet commerceTestSet = saveDefaultCommerceTestSet(testPrincipal.getId());

        int quantity1 = 2;
        int quantity2 = 1;
        int expectedStock1 = commerceTestSet.stocks().getFirst().getQuantity() - quantity1;
        int expectedStock2 = commerceTestSet.stocks().get(1).getQuantity() - quantity2;
        String paymentKey = "mock_payment_key";

        enqueueMockResponses(200, "{\"status\": \"DONE\", \"paymentKey\": \"mock_payment_key\"}", 1);
        enqueueMockResponses(500, "Internal Server Error", 1);
        String tossOrderId = executeOrderAndConfirmAndCancelSequence(testPrincipal, commerceTestSet, paymentKey, 2, 1);

        Payment payment = paymentRepository.findByTossOrderIdWithOrder(tossOrderId).orElseThrow();
        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.CANCEL_UNKNOWN_HOLD);
        assertThat(payment.getOrder().getOrderStatus()).isEqualTo(OrderStatus.CANCEL_REQUESTED);

        long stockId1 = commerceTestSet.stocks().getFirst().getId();
        long stockId2 = commerceTestSet.stocks().get(1).getId();
        assertThat(stockRepository.findById(stockId1).orElseThrow().getQuantity()).isEqualTo(expectedStock1);
        assertThat(stockRepository.findById(stockId2).orElseThrow().getQuantity()).isEqualTo(expectedStock2);
    }

    // =============================================================================
    // 시나리오 6: CANCEL_UNKNOWN_HOLD 건 조회 시 토스 측이 DONE(취소 안됨)일 때 재취소 및 재고 원복
    // =============================================================================
    @Test
    @DisplayName("[시나리오] 복구 스케줄러 가동 시 CANCEL_UNKNOWN_HOLD 건의 토스 상태가 DONE이면 취소를 재요청하여 성공시키고 재고를 원복한다.")
    void scheduler_Recovery_CancelUnknownHold_To_CancelSuccess_And_RollbackStock_Integration() throws Exception {
        // given: DB에 CANCEL_UNKNOWN_HOLD 상태의 결제/주문 데이터 및 차감된 재고 가상 적재
        CustomUserPrincipal testPrincipal = loginAsMember();
        CommerceTestSet commerceTestSet = saveDefaultCommerceTestSet(testPrincipal.getId());

        int quantity1 = 2;
        int quantity2 = 1;
        int expectedStock1 = commerceTestSet.stocks().getFirst().getQuantity();
        int expectedStock2 = commerceTestSet.stocks().get(1).getQuantity();
        String paymentKey = "mock_payment_key";

        enqueueMockResponses(200, "{\"status\": \"DONE\", \"paymentKey\": \"mock_payment_key\"}", 1);
        enqueueMockResponses(500, "Internal Server Error", 1);
        String tossOrderId = executeOrderAndConfirmAndCancelSequence(testPrincipal, commerceTestSet, paymentKey, quantity1, quantity2);

        Payment payment = paymentRepository.findByTossOrderIdWithOrder(tossOrderId).orElseThrow();
        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.CANCEL_UNKNOWN_HOLD);
        assertThat(payment.getOrder().getOrderStatus()).isEqualTo(OrderStatus.CANCEL_REQUESTED);


        enqueueMockResponses(200, "{\"status\": \"DONE\"}", 1);
        enqueueMockResponses(200, "{\"status\" : \"CANCELED \"}", 1);
        paymentRecoveryScheduler.recoverCancelRequestedPayments();

        // then: 데이터베이스 최종 상태 및 재고 복구(원복) 검증
        payment = paymentRepository.findByTossOrderIdWithOrder(tossOrderId).orElseThrow();
        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(payment.getOrder().getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);

        long stockId1 = commerceTestSet.stocks().getFirst().getId();
        long stockId2 = commerceTestSet.stocks().get(1).getId();
        assertThat(stockRepository.findById(stockId1).orElseThrow().getQuantity()).isEqualTo(expectedStock1);
        assertThat(stockRepository.findById(stockId2).orElseThrow().getQuantity()).isEqualTo(expectedStock2);
    }

    // =============================================================================
    // 시나리오 : CANCEL_UNKNOWN_HOLD 건 조회 시 토스 측이 이미 CANCELED(취소 완료)일 때 매핑 및 재고 원복
    // =============================================================================
    @Test
    @DisplayName("[시나리오] 복구 스케줄러 가동 시 CANCEL_UNKNOWN_HOLD 건의 토스 상태가 이미 CANCELED이면 취소 요청 없이 상태만 동기화하고 재고를 원복한다.")
    void scheduler_Recovery_CancelUnknownHold_AlreadyCanceled_To_CancelSuccess_And_RollbackStock_Integration() throws Exception {
        CustomUserPrincipal testPrincipal = loginAsMember();
        CommerceTestSet commerceTestSet = saveDefaultCommerceTestSet(testPrincipal.getId());

        int quantity1 = 2;
        int quantity2 = 1;
        int expectedStock1 = commerceTestSet.stocks().getFirst().getQuantity();
        int expectedStock2 = commerceTestSet.stocks().get(1).getQuantity();
        String paymentKey = "mock_payment_key";

        enqueueMockResponses(200, "{\"status\": \"DONE\", \"paymentKey\": \"mock_payment_key\"}", 1);
        enqueueMockResponses(500, "Internal Server Error", 1);
        String tossOrderId = executeOrderAndConfirmAndCancelSequence(testPrincipal, commerceTestSet, paymentKey, quantity1, quantity2);

        Payment payment = paymentRepository.findByTossOrderIdWithOrder(tossOrderId).orElseThrow();
        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.CANCEL_UNKNOWN_HOLD);
        assertThat(payment.getOrder().getOrderStatus()).isEqualTo(OrderStatus.CANCEL_REQUESTED);

        enqueueMockResponses(200, "{\"status\": \"CANCELED\"}", 1);

        paymentRecoveryScheduler.recoverCancelRequestedPayments();

        payment = paymentRepository.findByTossOrderIdWithOrder(tossOrderId).orElseThrow();
        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.CANCELLED);
        assertThat(payment.getOrder().getOrderStatus()).isEqualTo(OrderStatus.CANCELLED);

        long stockId1 = commerceTestSet.stocks().getFirst().getId();
        long stockId2 = commerceTestSet.stocks().get(1).getId();
        assertThat(stockRepository.findById(stockId1).orElseThrow().getQuantity()).isEqualTo(expectedStock1);
        assertThat(stockRepository.findById(stockId2).orElseThrow().getQuantity()).isEqualTo(expectedStock2);
    }

    private String executeOrderAndConfirmSequence(CustomUserPrincipal testPrincipal, CommerceTestSet commerceTestSet, String paymentKey, int quantity1, int quantity2)
            throws Exception {

        OrderItemDto orderItemDto1 = new OrderItemDto(commerceTestSet.items().getFirst().getId(), quantity1);
        OrderItemDto orderItemDto2 = new OrderItemDto(commerceTestSet.items().get(1).getId(), quantity2);
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(commerceTestSet.address().getId(), List.of(orderItemDto1, orderItemDto2));

        String jsonContent = objectMapper.writeValueAsString(createOrderRequest);

        // 주문 생성
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/orders")
                        .with(user(testPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(responseBody);
        JsonNode data = jsonNode.path("data");

        String tossOrderId = data.path("tossOrderId").asText();
        int amount = data.path("amount").asInt();


        PaymentConfirmRequest dto = new PaymentConfirmRequest(paymentKey, tossOrderId, amount);

        mockMvc.perform(post("/api/v1/payments/confirm")
                        .with(user(testPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.code").value("PAY-5001"))
                .andExpect(jsonPath("$.message").value(PaymentErrorCode.PAYMENT_UNKNOWN_HOLD.getMessage()));

        return tossOrderId;
    }

    private String executeOrderAndConfirmAndCancelSequence(CustomUserPrincipal testPrincipal, CommerceTestSet commerceTestSet, String paymentKey, int quantity1, int quantity2)
            throws Exception {

        OrderItemDto orderItemDto1 = new OrderItemDto(commerceTestSet.items().getFirst().getId(), quantity1);
        OrderItemDto orderItemDto2 = new OrderItemDto(commerceTestSet.items().get(1).getId(), quantity2);
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(commerceTestSet.address().getId(), List.of(orderItemDto1, orderItemDto2));

        String jsonContent = objectMapper.writeValueAsString(createOrderRequest);

        // 주문 생성
        MvcResult mvcResult = mockMvc.perform(post("/api/v1/orders")
                        .with(user(testPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andReturn();

        String responseBody = mvcResult.getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(responseBody);
        JsonNode data = jsonNode.path("data");

        String tossOrderId = data.path("tossOrderId").asText();
        int amount = data.path("amount").asInt();


        PaymentConfirmRequest dto = new PaymentConfirmRequest(paymentKey, tossOrderId, amount);

        mvcResult = mockMvc.perform(post("/api/v1/payments/confirm")
                        .with(user(testPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andDo(print())
                .andReturn();

        Payment payment = paymentRepository.findByTossOrderIdWithOrder(tossOrderId).orElseThrow();

        assertThat(payment.getPayStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(payment.getOrder().getOrderStatus()).isEqualTo(OrderStatus.CONFIRMED);

        mockMvc.perform(post("/api/v1/payments/cancel")
                        .with(user(testPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new PaymentCancelRequest(tossOrderId, payment.getCancelableAmount(), paymentKey, "테스트 취소"))))
                .andDo(print())
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.code").value("PAY-5001"))
                .andExpect(jsonPath("$.message").value(PaymentErrorCode.PAYMENT_UNKNOWN_HOLD.getMessage()));

        return tossOrderId;
    }

    private void enqueueMockResponses(int statusCode, String bodyMessage, int count) {
        for (int i = 1; i <= count; i++) {
            MockResponse response = new MockResponse()
                    .setResponseCode(statusCode)
                    .setHeader("Content-Type", "application/json");

            if (statusCode == 200) {
                response.setBody(bodyMessage);
            } else {
                response.setBody(bodyMessage + " - Count: " + i);
            }
            mockWebServer.enqueue(response);
        }
    }
    
}
