package com.example.i_commerce.domain.order.unit.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.i_commerce.domain.member.entity.Store;
import com.example.i_commerce.domain.member.repository.StoreRepository;
import com.example.i_commerce.domain.order.entity.Delivery;
import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.emuns.DeliveryStatus;
import com.example.i_commerce.domain.order.event.dto.DeliveryStatusChangedEvent;
import com.example.i_commerce.domain.order.exception.DeliveryErrorCode;
import com.example.i_commerce.domain.order.repository.DeliveryRepository;
import com.example.i_commerce.domain.order.service.SellerDeliveryService;
import com.example.i_commerce.domain.order.service.dto.DeliveryResponse;
import com.example.i_commerce.domain.order.service.dto.DeliveryShipRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.exception.AppException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class SellerDeliveryServiceTest {

    @Mock
    DeliveryRepository deliveryRepository;
    @Mock
    StoreRepository storeRepository;
    @Mock
    ApplicationEventPublisher publisher;

    @InjectMocks
    SellerDeliveryService sellerDeliveryService;

    @Nested
    @DisplayName("shipDelivery - 배송 처리 테스트")
    class ShipDeliveryTest {

        private final Long sellerId = 1L;
        private final Long storeId = 10L;
        private final Long deliveryId = 100L;
        private final Long orderId = 1000L;
        private final String trackingNumber = "TRK123456789";

        private final DeliveryShipRequest request = new DeliveryShipRequest(orderId, storeId, deliveryId, trackingNumber);

        @Test
        @DisplayName("[실패 케이스 1] 요청한 사장님ID와 가게의 사장님ID가 다를 때 -> STORE_FORBIDDEN 예외")
        void shipDelivery_StoreForbidden() {
            // given
            Store wrongStore = mock(Store.class);
            given(wrongStore.getSellerId()).willReturn(999L); // 다른 사장님 ID
            given(storeRepository.findById(storeId)).willReturn(Optional.of(wrongStore));

            // when & then
            assertThatThrownBy(() -> sellerDeliveryService.shipDelivery(sellerId, request))
                    .isInstanceOf(AppException.class)
                    .hasMessage(DeliveryErrorCode.STORE_FORBIDDEN.getMessage());

            verify(deliveryRepository, never()).findBWithOrderById(any());
        }

        @Test
        @DisplayName("[실패 케이스 2] 존재하지 않는 배달 ID를 요청했을 때 -> DELIVERY_NOT_FOUND 예외")
        void shipDelivery_DeliveryNotFound() {
            // given
            Store store = mock(Store.class);
            given(store.getSellerId()).willReturn(sellerId);
            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));
            given(deliveryRepository.findBWithOrderById(deliveryId)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> sellerDeliveryService.shipDelivery(sellerId, request))
                    .isInstanceOf(AppException.class)
                    .hasMessage(DeliveryErrorCode.DELIVERY_NOT_FOUND.getMessage());
        }

        @Test
        @DisplayName("[실패 케이스 3] 주문 ID가 일치하지 않으면 CANNOT_SHIP_STATUS 예외")
        void shipDelivery_InvalidOrderId_CannotShipStatus() {
            // given
            Store store = mock(Store.class);
            given(store.getSellerId()).willReturn(sellerId);
            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

            Order order = mock(Order.class);
            given(order.getId()).willReturn(999L); // request.orderId()와 일치하게 설정

            Delivery delivery = mock(Delivery.class);
            given(delivery.getOrder()).willReturn(order);
            given(deliveryRepository.findBWithOrderById(deliveryId)).willReturn(Optional.of(delivery));

            // when & then
            assertThatThrownBy(() -> sellerDeliveryService.shipDelivery(sellerId, request))
                    .isInstanceOf(AppException.class)
                    .hasMessage(DeliveryErrorCode.CANNOT_SHIP_STATUS.getMessage());

        }
        @Test
        @DisplayName("[실패 케이스] 배송 상태가 PREPARING이 아닐 때 CANNOT_SHIP_STATUS")
        void shipDelivery_NotPreparingStatus_CannotShipStatus() {
            // given
            Store store = mock(Store.class);
            given(store.getSellerId()).willReturn(sellerId);
            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

            Order order = mock(Order.class);
            given(order.getId()).willReturn(orderId); // 실패조건3을 피하기 위해 request.orderId()와 다르게 설정

            Delivery delivery = Delivery.builder()
                    .id(deliveryId)
                    .order(order)
                    .storeId(storeId)
                    .deliveryStatus(DeliveryStatus.CANCEL_REQUESTED)
                    .build();
            given(deliveryRepository.findBWithOrderById(deliveryId)).willReturn(Optional.of(delivery));

            // when & then
            assertThatThrownBy(() -> sellerDeliveryService.shipDelivery(sellerId, request))
                    .isInstanceOf(AppException.class)
                    .hasMessage(DeliveryErrorCode.CANNOT_SHIP_STATUS.getMessage());
            verify(publisher, never()).publishEvent(any(DeliveryStatusChangedEvent.class)); // 이벤트 1회 발행 검증
        }


        @Test
        @DisplayName("[성공 케이스] 모든 조건이 올바를 때 -> 운송장 등록 및 이벤트 발행")
        void shipDelivery_Success() {
            // given
            Store store = mock(Store.class);
            given(store.getSellerId()).willReturn(sellerId);
            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

            Order order = mock(Order.class);
            given(order.getId()).willReturn(orderId); // 실패조건3을 피하기 위해 request.orderId()와 다르게 설정

            Delivery delivery = Delivery.builder()
                    .id(deliveryId)
                    .order(order)
                    .storeId(storeId)
                    .deliveryStatus(DeliveryStatus.PREPARING)
                    .build();
            given(deliveryRepository.findBWithOrderById(deliveryId)).willReturn(Optional.of(delivery));

            // when
            ApiResponse<Void> response = sellerDeliveryService.shipDelivery(sellerId, request);

            // then
            assertThat(response.code()).isEqualTo("SUCCESS"); // ApiResponse 구조에 맞게 검증 (성공여부 메서드명 수정 가능)
            assertThat(delivery.getDeliveryStatus()).isEqualTo(DeliveryStatus.SHIPPING);
            verify(publisher, times(1)).publishEvent(any(DeliveryStatusChangedEvent.class)); // 이벤트 1회 발행 검증
        }
    }

    @Nested
    @DisplayName("getDeliveryList - 페이징 조회 테스트")
    class GetDeliveryListTest {

        private final Long sellerId = 1L;
        private final Long storeId = 10L;
        private final DeliveryStatus status = DeliveryStatus.SHIPPING;
        private final Pageable pageable = PageRequest.of(0, 10);

        @Test
        @DisplayName("[실패 케이스] 요청한 사장님ID와 가게의 사장님ID가 다를 때 -> DELIVERY_NOT_FOUND 예외")
        void getDeliveryList_StoreForbidden() {
            // given
            Store wrongStore = mock(Store.class);
            given(wrongStore.getSellerId()).willReturn(999L);
            given(storeRepository.findById(storeId)).willReturn(Optional.of(wrongStore));

            // when & then
            assertThatThrownBy(() -> sellerDeliveryService.getDeliveryList(sellerId, storeId, status, pageable))
                    .isInstanceOf(AppException.class)
                    .extracting("errorCode")
                    .isEqualTo(DeliveryErrorCode.DELIVERY_NOT_FOUND);

            verify(deliveryRepository, never()).findAllByStoreId(any(), any(), any());
        }

        @Test
        @DisplayName("[성공 케이스] 정상적인 페이징 및 필터링 조회 -> DTO 변환 확인")
        void getDeliveryList_Success() {
            // given
            Store store = mock(Store.class);
            given(store.getSellerId()).willReturn(sellerId);
            given(storeRepository.findById(storeId)).willReturn(Optional.of(store));

            // 가짜 딜리버리와 주문 세팅
            Order mockOrder = mock(Order.class);
            given(mockOrder.getId()).willReturn(555L);

            Delivery mockDelivery = mock(Delivery.class);
            given(mockDelivery.getId()).willReturn(100L);
            given(mockDelivery.getDeliveryStatus()).willReturn(DeliveryStatus.SHIPPING);
            given(mockDelivery.getCreatedAt()).willReturn(LocalDateTime.now());
            given(mockDelivery.getOrder()).willReturn(mockOrder);

            // Page 객체 생성
            Page<Delivery> deliveryPage = new PageImpl<>(List.of(mockDelivery), pageable, 1);
            given(deliveryRepository.findAllByStoreId(storeId, status, pageable)).willReturn(deliveryPage);

            // when
            ApiResponse<Page<DeliveryResponse>> response = sellerDeliveryService.getDeliveryList(sellerId, storeId, status, pageable);

            // then
            assertThat(response).isNotNull();
            Page<DeliveryResponse> resultPage = response.data(); // ApiResponse에 getData()가 있다고 가정

            assertThat(resultPage.getContent().size()).isEqualTo(1);

            // DTO로 누락 없이 정확하게 매핑되었는지 필드 개별 검증
            DeliveryResponse dto = resultPage.getContent().getFirst();
            assertThat(dto.orderId()).isEqualTo(555L);
            assertThat(dto.deliveryId()).isEqualTo(100L);
            assertThat(dto.status()).isEqualTo(DeliveryStatus.SHIPPING);

            verify(deliveryRepository, times(1)).findAllByStoreId(storeId, status, pageable);
        }
    }

}