package com.example.i_commerce.domain.order.unit.service;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.i_commerce.domain.order.entity.Delivery;
import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.OrderProduct;
import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.emuns.DeliveryStatus;
import com.example.i_commerce.domain.order.event.dto.PaymentApprovedEvent;
import com.example.i_commerce.domain.order.exception.PaymentErrorCode;
import com.example.i_commerce.domain.order.repository.DeliveryRepository;
import com.example.i_commerce.domain.order.repository.OrderRepository;
import com.example.i_commerce.domain.order.event.dto.DeliveryCancelRequestEvent;
import com.example.i_commerce.domain.order.service.DeliveryService;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.repository.ProductItemRepository;
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
class DeliveryServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock private DeliveryRepository deliveryRepository;
    @Mock private ProductItemRepository productItemRepository;

    @InjectMocks
    private DeliveryService deliveryService;

    private Delivery createDelivery(Long deliveryId, DeliveryStatus deliveryStatus) {
        return Delivery.builder()
                .id(deliveryId)
                .deliveryStatus(deliveryStatus)
                .build();
    }

    @Test
    @DisplayName("주문 상품들을 판매자별로 그룹화하여 배송 정보를 생성한다")
    void createDelivery_Success() {
        // 1. Given: 테스트 데이터 준비
        Long orderId = 1L;
        Long storeAId = 100L;
        Long storeBId = 200L;
        Long skuId1 = 10L;
        Long skuId2 = 20L;

        // 주문 상품 설정 (판매자 A 상품 1개, 판매자 B 상품 1개)
        Order order = mock(Order.class);
        OrderProduct item1 = mock(OrderProduct.class);
        OrderProduct item2 = mock(OrderProduct.class);

        given(item1.getProductSkuId()).willReturn(skuId1);
        given(item2.getProductSkuId()).willReturn(skuId2);
        given(order.getOrderProducts()).willReturn(List.of(item1, item2));

        // Event 모킹
        PaymentApprovedEvent event = mock(PaymentApprovedEvent.class);
        Payment payment = mock(Payment.class);
        given(event.orderId()).willReturn(orderId);

        // Repository 동작 정의
        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

        // 상품-판매자 매핑 모킹
        ProductItem productItem1 = mock(ProductItem.class);
        ProductItem productItem2 = mock(ProductItem.class);
        Product product1 = mock(Product.class);
        Product product2 = mock(Product.class);

        given(productItem1.getId()).willReturn(skuId1);
        given(productItem1.getProduct()).willReturn(product1);
        given(product1.getStoreId()).willReturn(storeAId);

        given(productItem2.getId()).willReturn(skuId2);
        given(productItem2.getProduct()).willReturn(product2);
        given(product2.getStoreId()).willReturn(storeBId);

        given(productItemRepository.findAllById(anyList()))
                .willReturn(List.of(productItem1, productItem2));

        given(deliveryRepository.save(any(Delivery.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // When
        deliveryService.createDelivery(event);

        // Then
        verify(deliveryRepository, times(2)).save(any(Delivery.class));
        // - 각 아이템에 배송 정보가 할당되었는지 확인
        verify(item1).assignDelivery(any(Delivery.class));
        verify(item2).assignDelivery(any(Delivery.class));
    }

    @Test
    @DisplayName("주문 정보가 없으면 AppException이 발생한다")
    void createDelivery_OrderNotFound() {
        // given
        PaymentApprovedEvent event = mock(PaymentApprovedEvent.class);
        Payment payment = mock(Payment.class);
        Order order = mock(Order.class);

        given(event.orderId()).willReturn(1L);
        given(orderRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        AppException e = assertThrows(AppException.class, () -> deliveryService.createDelivery(event));
        assertEquals("ORDER_NOT_FOUND", e.getErrorCode().toString());
    }

    @Test
    @DisplayName("배송 취소 성공: 배송 전 상태의 주문은 모두 취소 상태로 변경된다.")
    void cancelDelivery_success() {
        // given
        Long orderId = 1L;
        DeliveryCancelRequestEvent event = new DeliveryCancelRequestEvent(orderId);

        Order order = mock(Order.class);
        // 가상의 배송 전(READY) 객체 생성 (실제 엔티티 구현체에 맞게 빌더나 생성자 사용)
        Delivery delivery1 = createDelivery(orderId, DeliveryStatus.PREPARING);
        Delivery delivery2 = createDelivery(orderId, DeliveryStatus.PREPARING);
        List<Delivery> mockDeliveries = List.of(delivery1, delivery2);

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(order.getDeliveries()).willReturn(mockDeliveries);

        // when
        deliveryService.cancelDelivery(event);

        // then
        assertEquals(DeliveryStatus.CANCELLED, delivery1.getDeliveryStatus());
        assertEquals(DeliveryStatus.CANCELLED, delivery2.getDeliveryStatus());
    }

    @Test
    @DisplayName("배송 취소 실패: 이미 배송중(SHIPPING)인 배송 건이 포함되어 있으면 예외가 발생한다.")
    void cancelDelivery_fail_already_shipping() {
        // given
        Long orderId = 1L;
        DeliveryCancelRequestEvent event = new DeliveryCancelRequestEvent(orderId);

        Order order = mock(Order.class);

        Delivery delivery1 = createDelivery(orderId, DeliveryStatus.PREPARING);
        Delivery delivery2 = createDelivery(orderId, DeliveryStatus.SHIPPING);

        List<Delivery> mockDeliveries = List.of(delivery1, delivery2);

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(order.getDeliveries()).willReturn(mockDeliveries);

        // when & then
        assertThatThrownBy(() -> deliveryService.cancelDelivery(event))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(PaymentErrorCode.PAYMENT_CANCEL_IMPOSSIBLE_ALREADY_SHIPPED.getMessage());
    }

    @Test
    @DisplayName("배송 취소 실패: 이미 배송완료(ARRIVED)인 배송 건이 포함되어 있으면 예외가 발생한다.")
    void cancelDelivery_fail_already_arrived() {
        // given
        Long orderId = 1L;
        DeliveryCancelRequestEvent event = new DeliveryCancelRequestEvent(orderId);

        Order order = mock(Order.class);

        Delivery delivery = createDelivery(orderId, DeliveryStatus.ARRIVED); // 배송완료 상태
        List<Delivery> mockDeliveries = List.of(delivery);

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(order.getDeliveries()).willReturn(mockDeliveries);

        // when & then
        assertThatThrownBy(() -> deliveryService.cancelDelivery(event))
                .isInstanceOf(AppException.class)
                .hasMessageContaining(PaymentErrorCode.PAYMENT_CANCEL_IMPOSSIBLE_ALREADY_SHIPPED.getMessage());
    }
}
