package com.example.i_commerce.domain.order.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.then;

import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.service.DeliveryAddressService;
import com.example.i_commerce.domain.member.service.MemberService;
import com.example.i_commerce.domain.member.service.dto.DeliveryAddressSnapshot;
import com.example.i_commerce.domain.member.service.dto.MemberOrderInfo;
import com.example.i_commerce.domain.order.repository.OrderRepository;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.domain.order.service.dto.CreateOrderRequest;
import com.example.i_commerce.domain.order.service.dto.OrderItemDto;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.repository.ProductItemRepository;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    ProductItemRepository productItemRepository;

    @Mock
    OrderRepository orderRepository;

    @Mock
    PaymentRepository paymentRepository;

    @Mock
    MemberService memberService;

    @Mock
    DeliveryAddressService deliveryAddressService;

    @InjectMocks
    OrderService orderService;

    private MemberOrderInfo createMockMemberOrderInfo() {
        MemberOrderInfo info = mock(MemberOrderInfo.class);

        given(info.id()).willReturn(OrderFixture.MEMBER_ID);
        given(info.name()).willReturn("홍길동");
        given(info.phoneNumber()).willReturn("010-1234-1234");

        return info;
    }

    private DeliveryAddressSnapshot createMockDeliveryAddressSnapshot() {
        DeliveryAddressSnapshot info = mock(DeliveryAddressSnapshot.class);

        given(info.zipCode()).willReturn(OrderFixture.ZIP_CODE);
        given(info.roadAddress()).willReturn(OrderFixture.ADDRESS);
        given(info.detailAddress()).willReturn(OrderFixture.DETAIL_ADDRESS);

        return info;
    }

    private ProductItem createMockProductItem(Long id, int price, String name) {
        ProductItem item = mock(ProductItem.class);
        Product product = mock(Product.class);
        given(item.getId()).willReturn(id);
        given(item.getPrice()).willReturn(price);
        given(item.getProduct()).willReturn(product);
        given(product.getName()).willReturn(name);
        return item;
    }

    @Test
    @DisplayName("성공: 다중 상품 주문 시 총액이 정확히 계산되고 저장된다")
    void createOrder_success_multipleItems() {
        MemberOrderInfo memberOrderInfo = createMockMemberOrderInfo();
        DeliveryAddressSnapshot addressInfo = createMockDeliveryAddressSnapshot();

        OrderItemDto item1Dto = OrderFixture.createItemDto(100L, 2);
        OrderItemDto item2Dto = OrderFixture.createItemDto(200L, 3);
        CreateOrderRequest dto = OrderFixture.createOrderDto(OrderFixture.MEMBER_ID, OrderFixture.ADDRESS_ID, item1Dto, item2Dto);

        ProductItem p1 = createMockProductItem(100L, 10000, "상품1");
        ProductItem p2 = createMockProductItem(200L, 5000, "상품2");

        given(memberService.getMemberOrderInfo(OrderFixture.MEMBER_ID)).willReturn(memberOrderInfo);
        given(deliveryAddressService.getAddressSnapshot(OrderFixture.ADDRESS_ID, OrderFixture.MEMBER_ID))
                .willReturn(addressInfo);

        given(productItemRepository.findAllById(anyList()))
                .willReturn(List.of(p1, p2));

        given(orderRepository.save(any()))
                .willAnswer(invocation -> invocation.getArgument(0));
        given(paymentRepository.save(any()))
                .willAnswer(invocation -> invocation.getArgument(0));

        ApiResponse<?> response = orderService.createOrder(dto);

        assertEquals("SUCCESS", response.code());

        then(orderRepository).should().save(argThat(order ->
                order.getTotalProductAmount() == 35000 &&
//                        order.getZipCode().equals(OrderFixture.ZIP_CODE) &&
                        order.getOrderProducts().size() == 2
        ));

    }

    @Test
    @DisplayName("실패: 상품 중 하나라도 존재하지 않으면 예외가 발생한다")
    void createOrder_fail_itemNotFound() {
        CreateOrderRequest dto = OrderFixture.createOrderDto(OrderFixture.MEMBER_ID, OrderFixture.ADDRESS_ID, OrderFixture.createItemDto(999L, 1));

        given(memberService.getMemberOrderInfo(OrderFixture.MEMBER_ID)).willReturn(Optional.of(mock(MemberOrderInfo.class)).orElseThrow());
        given(productItemRepository.findAllById(anyList())).willReturn(List.of());

        AppException exception = assertThrows(AppException.class, () -> orderService.createOrder(dto));
        //assertEquals(ErrorCodeImpl.PRODUCT_NOT_FOUND, exception.getErrorCode());
    }


    public static class OrderFixture {
        public static final Long MEMBER_ID = 1L;
        public static final Long ADDRESS_ID = 1L;
        public static final String ZIP_CODE = "12345";
        public static final String ADDRESS = "서울시 강남구";
        public static final String DETAIL_ADDRESS = "101호";

        // 주문 요청 DTO 생성 헬퍼
        public static CreateOrderRequest createOrderDto(Long userId, Long addressId, OrderItemDto... items) {
            return new CreateOrderRequest(userId, addressId, List.of(items));
        }

        // 개별 아이템 DTO 생성 헬퍼
        public static OrderItemDto createItemDto(Long productId, int quantity) {
            return new OrderItemDto(productId, quantity);
        }
    }
}