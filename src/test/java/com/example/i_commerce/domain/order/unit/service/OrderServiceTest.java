
package com.example.i_commerce.domain.order.unit.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.mock;
import static org.mockito.BDDMockito.then;


import com.example.i_commerce.domain.member.service.delivery.DeliveryAddressService;
import com.example.i_commerce.domain.member.service.delivery.dto.DeliveryAddressSnapshot;
import com.example.i_commerce.domain.member.service.member.MemberService;
import com.example.i_commerce.domain.member.service.member.dto.MemberOrderInfo;
import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.repository.OrderProductRepository;
import com.example.i_commerce.domain.order.repository.OrderRepository;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.domain.order.service.OrderService;
import com.example.i_commerce.domain.order.service.dto.CreateOrderRequest;
import com.example.i_commerce.domain.order.service.dto.CreateOrderRequest.OrderItemDto;
import com.example.i_commerce.domain.order.service.dto.CreateOrderResponse;
import com.example.i_commerce.domain.order.service.dto.OrderDetailResponse;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.facade.StockFacade;
import com.example.i_commerce.domain.product.repository.ProductItemRepository;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.exception.AppException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

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

    @Mock
    OrderProductRepository orderProductRepository;

    @Mock
    StockFacade stockFacade;

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

    private Payment createMockPayment(Long id, LocalDateTime createdAt) {
        Payment payment = Payment.builder().build(); // 또는 빌더 사용

        ReflectionTestUtils.setField(payment, "id", id);
        ReflectionTestUtils.setField(payment, "createdAt", createdAt);

        return payment;
    }


    @Test
    @DisplayName("성공: 다중 상품 주문 시 총액이 정확히 계산되고 저장된다")
    void createOrder_success_multipleItems() {
        MemberOrderInfo memberOrderInfo = createMockMemberOrderInfo();
        DeliveryAddressSnapshot addressInfo = createMockDeliveryAddressSnapshot();

        OrderItemDto item1Dto = OrderFixture.createItemDto(100L, 2);
        OrderItemDto item2Dto = OrderFixture.createItemDto(200L, 3);
        CreateOrderRequest dto = OrderFixture.createOrderDto(OrderFixture.ADDRESS_ID, item1Dto, item2Dto);

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

        ApiResponse<CreateOrderResponse> response = orderService.createOrder(OrderFixture.MEMBER_ID, dto);

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
        CreateOrderRequest dto = OrderFixture.createOrderDto(OrderFixture.ADDRESS_ID, OrderFixture.createItemDto(999L, 1));

        given(memberService.getMemberOrderInfo(OrderFixture.MEMBER_ID)).willReturn(Optional.of(mock(MemberOrderInfo.class)).orElseThrow());
        given(productItemRepository.findAllById(anyList())).willReturn(List.of());

        AppException exception = assertThrows(AppException.class, () -> orderService.createOrder(OrderFixture.MEMBER_ID, dto));
        assertEquals(ProductErrorCode.PRODUCT_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("주문 상세 조회 성공 - 최신 결제 정보가 포함되어야 한다")
    void getOrderDetail_Success() {
        // given
        Long orderId = 1L;
        Long userId = 10L;

        Order order = mock(Order.class);
        Payment payment1 = createMockPayment(1L, LocalDateTime.now().minusHours(2));
        Payment payment2 = createMockPayment(2L, LocalDateTime.now()) ;

        ReflectionTestUtils.setField(payment2, "tossOrderId", "toss_order_id");
        List<Payment> mockPayments = List.of(payment1, payment2);
        String tossOrderId = "toss_order_id";


        given(order.getId()).willReturn(orderId);
        given(orderProductRepository.findAllByOrderId(orderId)).willReturn(List.of());
        given(orderRepository.findByIdAndUserId(orderId, userId)).willReturn(Optional.of(order));
        given(order.getPayments()).willReturn(mockPayments);

        // when
        OrderDetailResponse response = orderService.getOrderDetail(orderId, userId);

        // then14
        assertNotNull(response);
        assertEquals(tossOrderId, response.paymentInfo().tossOrderId());
    }

    @Test
    @DisplayName("주문 상세 조회 실패 - 타인의 주문을 조회하면 예외가 발생한다")
    void getOrderDetail_Fail_InvalidUser() {
        // given
        Long orderId = 1L;
        Long invalidUserId = 999L; // 잘못된 사용자 ID

        given(orderRepository.findByIdAndUserId(orderId, invalidUserId))
                .willReturn(Optional.empty()); // 찾을 수 없음 반환

        // when & then
        AppException e = assertThrows(AppException.class, () -> orderService.getOrderDetail(orderId, invalidUserId));
        assertEquals("ORDER_NOT_FOUND", e.getErrorCode().toString());
    }

    public static class OrderFixture {
        public static final Long MEMBER_ID = 1L;
        public static final Long ADDRESS_ID = 1L;
        public static final String ZIP_CODE = "12345";
        public static final String ADDRESS = "서울시 강남구";
        public static final String DETAIL_ADDRESS = "101호";

        // 주문 요청 DTO 생성 헬퍼
        public static CreateOrderRequest createOrderDto(Long addressId, OrderItemDto... items) {
            return new CreateOrderRequest(addressId, List.of(items));
        }

        // 개별 아이템 DTO 생성 헬퍼
        public static OrderItemDto createItemDto(Long productId, int quantity) {
            return new OrderItemDto(productId, quantity);
        }
    }
}
