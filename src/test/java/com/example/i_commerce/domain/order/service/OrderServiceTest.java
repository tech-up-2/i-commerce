package com.example.i_commerce.domain.order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.example.i_commerce.domain.member.entity.DeliveryAddress;
import com.example.i_commerce.domain.member.entity.Member;
import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.order.repository.OrderRepository;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    ProductItemRepository productItemRepository;

    @Mock
    OrderRepository orderRepository;

    @InjectMocks
    OrderService orderService;

    private Member createMockMember() {
        Member member = mock(Member.class);
        DeliveryAddress address = mock(DeliveryAddress.class);
        when(address.getIsDefault()).thenReturn(true);
        when(address.getZipCode()).thenReturn(OrderFixture.ZIP_CODE);
        when(address.getRoadAddress()).thenReturn(OrderFixture.ADDRESS);
        when(address.getDetailAddress()).thenReturn(OrderFixture.DETAIL_ADDRESS);
        when(member.getDeliveryAddresses()).thenReturn(List.of(address));
        return member;
    }

    private ProductItem createMockProductItem(Long id, int price, String name) {
        ProductItem item = mock(ProductItem.class);
        Product product = mock(Product.class);
        when(item.getId()).thenReturn(id);
        when(item.getPrice()).thenReturn(price);
        when(item.getProduct()).thenReturn(product);
        when(product.getName()).thenReturn(name);
        return item;
    }

    @Test
    @DisplayName("성공: 다중 상품 주문 시 총액이 정확히 계산되고 저장된다")
    void createOrder_success_multipleItems() {
        OrderItemDto item1Dto = OrderFixture.createItemDto(100L, 2);
        OrderItemDto item2Dto = OrderFixture.createItemDto(200L, 3);
        CreateOrderRequest dto = OrderFixture.createOrderDto(OrderFixture.MEMBER_ID, item1Dto, item2Dto);

        Member member = createMockMember();

        ProductItem p1 = createMockProductItem(100L, 10000, "상품1");
        ProductItem p2 = createMockProductItem(200L, 5000, "상품2");

        when(memberRepository.findById(OrderFixture.MEMBER_ID)).thenReturn(Optional.of(member));
        when(productItemRepository.findAllById(anyList())).thenReturn(List.of(p1, p2));

        ApiResponse<?> response = orderService.createOrder(dto);

        assertEquals("SUCCESS", response.code());

        // 합계 검증: (10000 * 2) + (5000 * 3) = 35000
        verify(orderRepository).save(argThat(order ->
                order.getTotalProductAmount() == 35000 &&
                        order.getZipCode().equals(OrderFixture.ZIP_CODE) &&
                        order.getOrderProducts().size() == 2
        ));
    }

    @Test
    @DisplayName("실패: 상품 중 하나라도 존재하지 않으면 예외가 발생한다")
    void createOrder_fail_itemNotFound() {
        CreateOrderRequest dto = OrderFixture.createOrderDto(OrderFixture.MEMBER_ID, OrderFixture.createItemDto(999L, 1));

        when(memberRepository.findById(OrderFixture.MEMBER_ID)).thenReturn(Optional.of(mock(Member.class)));
        when(productItemRepository.findAllById(anyList())).thenReturn(List.of());

        AppException exception = assertThrows(AppException.class, () -> orderService.createOrder(dto));
        //assertEquals(ErrorCodeImpl.PRODUCT_NOT_FOUND, exception.getErrorCode());
    }


    public static class OrderFixture {
        public static final Long MEMBER_ID = 1L;
        public static final String ZIP_CODE = "12345";
        public static final String ADDRESS = "서울시 강남구";
        public static final String DETAIL_ADDRESS = "101호";

        // 주문 요청 DTO 생성 헬퍼
        public static CreateOrderRequest createOrderDto(Long userId, OrderItemDto... items) {
            return new CreateOrderRequest(userId, List.of(items));
        }

        // 개별 아이템 DTO 생성 헬퍼
        public static OrderItemDto createItemDto(Long productId, int quantity) {
            return new OrderItemDto(productId, quantity);
        }
    }
}