package com.example.i_commerce.domain.order.integration.concurrency_test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.example.i_commerce.common.OrderIntegrationTestSupport;
import com.example.i_commerce.domain.member.entity.Store;
import com.example.i_commerce.domain.member.entity.enums.StoreStatus;
import com.example.i_commerce.domain.member.repository.StoreRepository;
import com.example.i_commerce.domain.order.entity.Delivery;
import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.emuns.DeliveryStatus;
import com.example.i_commerce.domain.order.exception.DeliveryErrorCode;
import com.example.i_commerce.domain.order.service.SellerDeliveryService;
import com.example.i_commerce.domain.order.service.dto.DeliveryShipRequest;
import com.example.i_commerce.domain.order.support.MockCourierComponent;
import com.example.i_commerce.global.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"test", "k6"})
public class DeliveryLifecycleIntegrationTest extends OrderIntegrationTestSupport {

    @Autowired
    private MockCourierComponent mockCourierComponent;

    @Autowired
    private SellerDeliveryService sellerDeliveryService;

    @Autowired
    private StoreRepository storeRepository;

    private Long testOrderId;
    private Long testSellerId = 1L;
    private Long testStoreId;
    private Long preparingDeliveryId;
    private Long shippingDeliveryId;

    @BeforeEach
    void setUp() {
        Order order = Order.builder()
                .userId(1L)
                .totalProductAmount(10000)
                .totalPayAmount(10000)
                .build();

        Order savedOrder = orderRepository.save(order);
        testOrderId = savedOrder.getId();

        Store savedStore = storeRepository.save(Store.builder()
                .sellerId(testSellerId)
                .phoneNumber("01012341234")
                .storeName("상점")
                .storeStatus(StoreStatus.OPEN)
                .build());
        testStoreId = savedStore.getId();

        Delivery preparing = Delivery.builder()
                .storeId(testStoreId)
                .deliveryStatus(DeliveryStatus.PREPARING)
                .order(savedOrder)
                .build();

        Delivery shipping = Delivery.builder()
                .storeId(testStoreId)
                .deliveryStatus(DeliveryStatus.SHIPPING)
                .order(savedOrder)
                .build();

        savedOrder.getDeliveries().add(preparing);
        savedOrder.getDeliveries().add(shipping);

        Order updated = orderRepository.save(savedOrder);

        preparingDeliveryId = updated.getDeliveries().getFirst().getId();
        shippingDeliveryId = updated.getDeliveries().getLast().getId();
    }

    @Test
    @DisplayName("판매자가 이미 배송중인 상품을 다시 출고시도하면 CANNOT_SHIP_STATUS 예외가 발생한다")
    void cannotShipWhenNotPreparing() {
        DeliveryShipRequest shipRequest = new DeliveryShipRequest(testOrderId, testStoreId, shippingDeliveryId, "TRACK-0001");

        AppException ex = assertThrows(AppException.class, () ->
                sellerDeliveryService.shipDelivery(testSellerId, shipRequest)
        );

        assertEquals(DeliveryErrorCode.CANNOT_SHIP_STATUS, ex.getErrorCode());
    }

    @Test
    @DisplayName("판매자가 배송 시작시 상태가 SHIPPING으로 바뀌고, 가상 택배사가 배송 완료 처리 후 ARRIVED가 된다")
    void shipThenCourierCompletesDelivery() throws InterruptedException {
        DeliveryShipRequest shipRequest = new DeliveryShipRequest(testOrderId, testStoreId, preparingDeliveryId, "TRACK-0002");

        sellerDeliveryService.shipDelivery(testSellerId, shipRequest);

        Delivery afterShip = deliveryRepository.findById(preparingDeliveryId).orElseThrow();
        assertThat(afterShip.getDeliveryStatus()).isEqualTo(DeliveryStatus.SHIPPING);

        // MockCourierComponent sleeps ~500-1000ms before calling completeDelivery.
        // 폴링으로 ARRIVED 상태를 최대 5초까지 기다린다.
        long timeoutMs = 5000;
        long start = System.currentTimeMillis();
        boolean arrived = false;
        while (System.currentTimeMillis() - start < timeoutMs) {
            Delivery current = deliveryRepository.findById(preparingDeliveryId).orElseThrow();
            if (current.getDeliveryStatus() == DeliveryStatus.ARRIVED) {
                arrived = true;
                break;
            }
            Thread.sleep(200);
        }

        assertThat(arrived).isTrue();
        Delivery finalDelivery = deliveryRepository.findById(preparingDeliveryId).orElseThrow();
        assertThat(finalDelivery.getDeliveryStatus()).isEqualTo(DeliveryStatus.ARRIVED);
    }

}
