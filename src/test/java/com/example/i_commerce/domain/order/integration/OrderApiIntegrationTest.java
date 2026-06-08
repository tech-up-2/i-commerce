package com.example.i_commerce.domain.order.integration;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.i_commerce.common.OrderIntegrationTestSupport;
import com.example.i_commerce.domain.order.client.PaymentClient;
import com.example.i_commerce.domain.order.service.PaymentService;
import com.example.i_commerce.domain.order.service.dto.CreateOrderRequest;
import com.example.i_commerce.domain.order.service.dto.CreateOrderRequest.OrderItemDto;
import com.example.i_commerce.domain.order.service.dto.PaymentCancelRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentConfirmRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentDetailResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.List;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.MockResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.i_commerce.domain.order.config.TestWebClientConfig;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@Transactional
class OrderApiIntegrationTest extends OrderIntegrationTestSupport {

    @Autowired
    private PaymentClient tossPaymentClient;
    @Autowired
    private PaymentService paymentService;

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

    @Test
    @DisplayName("인증된 사용자의 상품 주문, 결제, 결제 취소까지의 전체 라이프사이클을 검증한다.")
    void orderAndPaymentLifecycleScenario() throws Exception {

        CustomUserPrincipal testPrincipal = loginAsMember();

        CommerceTestSet commerceTestSet = saveDefaultCommerceTestSet(testPrincipal.getId());

        OrderItemDto orderItemDto1 = new OrderItemDto(commerceTestSet.items().getFirst().getId(), 2);
        OrderItemDto orderItemDto2 = new OrderItemDto(commerceTestSet.items().get(1).getId(), 1);
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

        JsonNode root = objectMapper.readTree(responseBody);
        JsonNode dataNode = root.path("data");

        // 결제: MockWebServer에 결제 승인 응답을 준비합니다.
        String paymentKey = "toss_1_123";
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody("{\"paymentKey\": \"" + paymentKey + "\"}"));

        long orderId =  dataNode.path("orderId").asLong();

        PaymentDetailResponse paymentDetailResponse = paymentService.getPaymentDetails(testPrincipal.getId(), orderId);
        // 사이 프론트 생략

        String tossOrderId = paymentDetailResponse.tossOrderId();
        PaymentConfirmRequest paymentConfirmRequest = new PaymentConfirmRequest(paymentKey,
            paymentDetailResponse.tossOrderId(), paymentDetailResponse.amount());


        jsonContent = objectMapper.writeValueAsString(paymentConfirmRequest);

        mockMvc.perform(post("/api/v1/payments/confirm")
                .with(user(testPrincipal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andReturn();

        // 주문 목록 조회
        mvcResult = mockMvc.perform(get("/api/v1/orders")
                .with(user(testPrincipal))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andExpect(jsonPath("$.data").isArray())
            .andExpect(jsonPath("$.data.length()").value(1)).andReturn();

        responseBody = mvcResult.getResponse().getContentAsString();
        root = objectMapper.readTree(responseBody);
        dataNode = root.path("data");

        orderId = dataNode.get(0).path("orderId").asLong();

        // 상세 내역 조회
        mvcResult = mockMvc.perform(get("/api/v1/orders/" + orderId)
                .with(user(testPrincipal))
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS")).andReturn();

        responseBody = mvcResult.getResponse().getContentAsString();
        root = objectMapper.readTree(responseBody);
        dataNode = root.path("data");
        dataNode.path("paymentInfo").path("paymentId").asLong();

        // 결제 취소
        PaymentCancelRequest paymentCancelRequest = new PaymentCancelRequest(tossOrderId, 10000,
            paymentKey, "단순 변심");

        jsonContent = objectMapper.writeValueAsString(paymentCancelRequest);

        // 결제 취소: MockWebServer에 취소 응답을 준비합니다.
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody("{\"paymentKey\": \"" + paymentKey + "\"}"));

        mockMvc.perform(post("/api/v1/payments/cancel")
                .with(user(testPrincipal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andReturn();


    }
}
