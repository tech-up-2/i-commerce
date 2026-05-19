package com.example.i_commerce.domain.order.integration;

import com.example.i_commerce.common.IntegrationTestSupport;
import com.example.i_commerce.domain.member.entity.DeliveryAddress;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.entity.enums.Gender;
import com.example.i_commerce.domain.member.repository.DeliveryAddressRepository;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.tools.DataEncryptor;
import com.example.i_commerce.domain.order.service.dto.CreateOrderRequest;
import com.example.i_commerce.domain.order.service.dto.CreateOrderRequest.OrderItemDto;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
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


    protected CustomUserPrincipal loginAsMember() {
        // 1. 실제 DB 회원 생성 및 저장 (기본 PK가 1L로 쌓이도록 보장)
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

        // 2. 주입용 CustomUserPrincipal 생성 및 반환
        return new CustomUserPrincipal(
                PrincipalType.MEMBER,
                1L,
                "testMember",
                "",
                List.of(new SimpleGrantedAuthority("ROLE_MEMBER"))
        );
    }

    @Test
    @DisplayName("인증된 사용자는 상품 ID와 수량을 통해 주문을 생성할 수 있다.")
    void 주문_생성_통합테스트() throws Exception {

        CustomUserPrincipal testPrincipal = loginAsMember();
        DeliveryAddress address = DeliveryAddress.builder()
                .memberId(1L)
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

        OrderItemDto orderItemDto1 = new OrderItemDto(1L, 2);
        OrderItemDto orderItemDto2 = new OrderItemDto(2L, 1);
        CreateOrderRequest requestDto = new CreateOrderRequest(savedAddress.getId(), List.of(orderItemDto1, orderItemDto2));

        String jsonContent = objectMapper.writeValueAsString(requestDto);

        // when & then
        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .with(user(testPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonContent))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("SUCCESS"));
    }
}
