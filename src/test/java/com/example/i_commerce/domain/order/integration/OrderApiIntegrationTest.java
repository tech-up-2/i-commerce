package com.example.i_commerce.domain.order.integration;

// ...removed RestTemplate/Mockito stubs; replaced with MockWebServer enqueues
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.i_commerce.common.IntegrationTestSupport;
import com.example.i_commerce.domain.member.entity.DeliveryAddress;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.repository.DeliveryAddressRepository;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.order.repository.OrderRepository;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.domain.order.service.PaymentService;
import com.example.i_commerce.domain.order.service.dto.CreateOrderRequest;
import com.example.i_commerce.domain.order.service.dto.CreateOrderRequest.OrderItemDto;
import com.example.i_commerce.domain.order.service.dto.PaymentCancelRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentConfirmRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentDetailResponse;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.enums.ProductItemStatus;
import com.example.i_commerce.domain.product.entity.enums.ProductOptionType;
import com.example.i_commerce.domain.product.entity.enums.ProductStatus;
import com.example.i_commerce.domain.product.entity.Stock;
import com.example.i_commerce.domain.product.entity.enums.StockStatus;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.domain.product.repository.ProductItemRepository;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.domain.product.repository.StockRepository;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
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
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import com.example.i_commerce.domain.order.config.TestWebClientConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import com.example.i_commerce.domain.order.client.TossPaymentClient;


@Transactional
class OrderApiIntegrationTest extends IntegrationTestSupport {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Autowired
    private TossPaymentClient tossPaymentClient;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PaymentRepository paymentRepository;
    @Autowired
    private DeliveryAddressRepository deliveryAddressRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ProductItemRepository productItemRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private DataEncryptor dataEncryptor;
    @Autowired
    private PaymentService paymentService;

    private static MockWebServer mockWebServer;

    @BeforeAll
    static void startServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.start(8089); // @TestPropertySource에 지정한 포트와 일치시킵니다.
    }

    @AfterAll
    static void stopServer() throws IOException {
        if (mockWebServer != null) {
            mockWebServer.shutdown();
        }
    }

    @BeforeEach
    void setUp() {
        // 시나리오 테스트 시작 전, 혹시 남아있을 가짜 응답 대기열을 완전히 비워줍니다.
        mockWebServer.setDispatcher(new okhttp3.mockwebserver.QueueDispatcher());
        // 테스트의 TossPaymentClient가 MockWebServer를 바라보도록 WebClient를 교체합니다.
        WebClient targetWebClient = TestWebClientConfig.createTestWebClient(mockWebServer.url("/").toString());
        ReflectionTestUtils.setField(tossPaymentClient, "tossWebClient", targetWebClient);
    }

    protected CustomUserPrincipal loginAsMember() {
        Member member = Member.builder()
            .name(dataEncryptor.encrypt("테스트회원"))
            .phoneNumber(dataEncryptor.encrypt("010-1234-5678"))
            .emailHash("hashedEmail")
            .emailEncrypted(dataEncryptor.encrypt("test@example.com"))
            .password("password")
            .sex(Gender.MALE)
            .birthday(dataEncryptor.encrypt("20431123"))
            .build();
        memberRepository.save(member);

        return new CustomUserPrincipal(
            PrincipalType.MEMBER,
            member.getId(),
            List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
        );
    }

    @Test
    @DisplayName("인증된 사용자의 상품 주문, 결제, 결제 취소까지의 전체 라이프사이클을 검증한다.")
    void orderAndPaymentLifecycleScenario() throws Exception {

        CustomUserPrincipal testPrincipal = loginAsMember();

        DeliveryAddress address = DeliveryAddress.builder()
            .memberId(testPrincipal.getId())
            .label("집")
            .recipientName(dataEncryptor.encrypt("홍길동"))
            .recipientPhone(dataEncryptor.encrypt("01012345678"))
            .zipCode(dataEncryptor.encrypt("12345"))
            .roadAddress(dataEncryptor.encrypt("서울특별시 강남구 테헤란로"))
            .detailAddress(dataEncryptor.encrypt("101호"))
            .build();

        DeliveryAddress savedAddress = deliveryAddressRepository.save(address);

        Category category = categoryRepository.save(Category.builder()
            .name("전자기기")
            .depth(0)
            .build()
        );

        Product product = productRepository.save(Product.builder()
            .name("최고급 맥북 프로")
            .storeId(1L)
            .optionType(ProductOptionType.NONE)
            .status(ProductStatus.ON_SALE)
            .category(category)
            .build());

        ProductItem item1 = ProductItem.builder()
            .product(product)
            .price(1500000)
            .status(ProductItemStatus.ON_SALE)
            .sku("asdf")
            .mainImageUrl("")
            .displayOptionName("")
            .build();

        ProductItem item2 = ProductItem.builder()
            .product(product)
            .price(50000)
            .status(ProductItemStatus.ON_SALE)
            .sku("qwer")
            .mainImageUrl("")
            .displayOptionName("")
            .build();

        productItemRepository.saveAll(List.of(item1, item2));

        Stock stock1 = stockRepository.save(Stock.builder()
            .productItem(item1)
            .quantity(5)
            .status(StockStatus.IN_STOCK)
            .build()
        );

        Stock stock2 = stockRepository.save(Stock.builder()
            .productItem(item2)
            .quantity(5)
            .status(StockStatus.IN_STOCK)
            .build()
        );

        OrderItemDto orderItemDto1 = new OrderItemDto(item1.getId(), 2);
        OrderItemDto orderItemDto2 = new OrderItemDto(item2.getId(), 1);
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(savedAddress.getId(),
            List.of(orderItemDto1, orderItemDto2));

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
            .setHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody("{\"paymentKey\": \"" + paymentKey + "\"}"));

        Long orderId =  dataNode.path("orderId").asLong();

        PaymentDetailResponse paymentDetailResponse = paymentService.getPaymentDetails(testPrincipal.getId(), orderId);
        // 사이 프론트 생략

        String tossOrderId = paymentDetailResponse.tossOrderId();
        PaymentConfirmRequest paymentConfirmRequest = new PaymentConfirmRequest(paymentKey,
            paymentDetailResponse.tossOrderId(), paymentDetailResponse.amount());


        jsonContent = objectMapper.writeValueAsString(paymentConfirmRequest);

        mvcResult = mockMvc.perform(post("/api/v1/payments/confirm")
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
        Long paymentId = dataNode.path("paymentInfo").path("paymentId").asLong();

        // 결제 취소
        PaymentCancelRequest paymentCancelRequest = new PaymentCancelRequest(tossOrderId, 10000,
            paymentKey, "단순 변심");

        jsonContent = objectMapper.writeValueAsString(paymentCancelRequest);

        // 결제 취소: MockWebServer에 취소 응답을 준비합니다.
        mockWebServer.enqueue(new MockResponse()
            .setResponseCode(200)
            .setHeader(org.springframework.http.HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .setBody("{\"paymentKey\": \"" + paymentKey + "\"}"));

        mvcResult = mockMvc.perform(post("/api/v1/payments/cancel")
                .with(user(testPrincipal))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("SUCCESS"))
            .andReturn();


    }
}
