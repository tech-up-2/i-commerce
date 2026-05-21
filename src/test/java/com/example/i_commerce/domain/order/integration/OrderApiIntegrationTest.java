package com.example.i_commerce.domain.order.integration;

import com.example.i_commerce.common.IntegrationTestSupport;
import com.example.i_commerce.domain.member.entity.DeliveryAddress;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.repository.DeliveryAddressRepository;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.Payment;
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
import com.example.i_commerce.domain.product.entity.ProductItemStatus;
import com.example.i_commerce.domain.product.entity.ProductOptionType;
import com.example.i_commerce.domain.product.entity.ProductStatus;
import com.example.i_commerce.domain.product.entity.Stock;
import com.example.i_commerce.domain.product.entity.StockStatus;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.domain.product.repository.ProductItemRepository;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.domain.product.repository.StockRepository;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal.PrincipalType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@Transactional
class OrderApiIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired private MemberRepository memberRepository;
    @Autowired private DeliveryAddressRepository deliveryAddressRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private ProductItemRepository productItemRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private StockRepository stockRepository;
    @Autowired private DataEncryptor dataEncryptor;
    @Autowired private PaymentService paymentService;

    @MockitoBean
    RestTemplate restTemplate;
    @Autowired
    private OrderRepository orderRepository;


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
                "testMember",
                "",
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
        CreateOrderRequest createOrderRequest = new CreateOrderRequest(savedAddress.getId(), List.of(orderItemDto1, orderItemDto2));

        String jsonContent = objectMapper.writeValueAsString(createOrderRequest);

        // 주문 생성
        MvcResult mvcResult =  mockMvc.perform(post("/api/v1/orders")
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


        // 결제
        String paymentKey = "toss_1_123";
        Map<String, Object> tossResponseBody = new HashMap<>();
        tossResponseBody.put("paymentKey", paymentKey);
        ResponseEntity<Map> responseEntity = new ResponseEntity<>(tossResponseBody, HttpStatus.OK);

        given(restTemplate.postForEntity(anyString(), any(), eq(Map.class)))
                .willReturn(responseEntity);

        Long firstPaymentId =  dataNode.path("paymentId").asLong();

        PaymentDetailResponse paymentDetailResponse = paymentService.getPaymentDetails(testPrincipal.getId(), firstPaymentId);
        // 사이 프론트 생략
        PaymentConfirmRequest paymentConfirmRequest = new PaymentConfirmRequest(paymentKey, paymentDetailResponse.tossOrderId(), paymentDetailResponse.amount());

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
        mvcResult =  mockMvc.perform(get("/api/v1/orders")
                        .with(user(testPrincipal))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1)).andReturn();


        responseBody = mvcResult.getResponse().getContentAsString();
        root = objectMapper.readTree(responseBody);
        dataNode = root.path("data");

        long orderId = dataNode.get(0).path("orderId").asLong();

        // 상세 내역 조회
        mvcResult =  mockMvc.perform(get("/api/v1/orders/" + orderId)
                        .with(user(testPrincipal))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS")).andReturn();


        responseBody = mvcResult.getResponse().getContentAsString();
        root = objectMapper.readTree(responseBody);
        dataNode = root.path("data");
        Long paymentId = dataNode.path("paymentInfo").path("paymentId").asLong();


        // 결제 취소
        PaymentCancelRequest paymentCancelRequest = new PaymentCancelRequest(paymentId, 10000, paymentKey, "단순 변심");

        jsonContent = objectMapper.writeValueAsString(paymentCancelRequest);

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
