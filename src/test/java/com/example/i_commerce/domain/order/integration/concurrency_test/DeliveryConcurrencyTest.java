package com.example.i_commerce.domain.order.integration.concurrency_test;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import com.example.i_commerce.common.IntegrationTestSupport;
import com.example.i_commerce.domain.member.entity.Store;
import com.example.i_commerce.domain.member.entity.enums.StoreStatus;
import com.example.i_commerce.domain.member.repository.StoreRepository;
import com.example.i_commerce.domain.order.client.PaymentClient;
import com.example.i_commerce.domain.order.client.TossPaymentClient;
import com.example.i_commerce.domain.order.entity.Delivery;
import com.example.i_commerce.domain.order.entity.Order;
import com.example.i_commerce.domain.order.entity.Payment;
import com.example.i_commerce.domain.order.entity.emuns.DeliveryStatus;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.order.entity.emuns.PaymentStatus;
import com.example.i_commerce.domain.order.exception.DeliveryErrorCode;
import com.example.i_commerce.domain.order.facade.PaymentFacade;
import com.example.i_commerce.domain.order.repository.DeliveryRepository;
import com.example.i_commerce.domain.order.repository.OrderRepository;
import com.example.i_commerce.domain.order.repository.PaymentRepository;
import com.example.i_commerce.domain.order.service.SellerDeliveryService;
import com.example.i_commerce.domain.order.service.dto.DeliveryShipRequest;
import com.example.i_commerce.domain.order.service.dto.PaymentCancelRequest;
import com.example.i_commerce.domain.product.event.listener.StockEventListener;
import com.example.i_commerce.global.exception.AppException;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class DeliveryConcurrencyTest extends IntegrationTestSupport {

    @Autowired
    private PaymentFacade paymentFacade; // 결제 취소 파사드

    @Autowired
    private SellerDeliveryService sellerDeliveryService; // 판매자 배송 서비스

    @Autowired
    private DeliveryRepository deliveryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private StoreRepository storeRepository;

    @MockitoBean // 스프링 부트 3.4+ 버전 기준 (구버전은 @MockBean 사용)
    private PaymentClient tossPaymentClient;

//    @MockitoBean
//    private ApplicationEventPublisher publisher;

    @MockitoBean
    private StockEventListener stockEventListener;

    private Long testOrderId;
    private Long testSellerId;
    private Long testStoreId;
    private Long testDeliveryId;
    private String testTossOrderId;
    private String testPaymentKey = "toss-payment-key-12345";

    @BeforeEach
    void setUp() {
        // 테스트용 데이터 세팅 (Order -> Delivery -> Payment 구조)
        Order order = Order.builder()
                .userId(1L)
                .totalProductAmount(50000)
                .totalPayAmount(50000)
                .orderStatus(OrderStatus.CONFIRMED)
                .build();

        Order savedOrder = orderRepository.save(order);
        testOrderId = savedOrder.getId();

        // Delivery를 생성하고 Order에 추가한 뒤 저장 (cascade 연쇄저장)
        testSellerId = 1L;


        Store savedStore = storeRepository.save(Store.builder()
                .sellerId(testSellerId)
                .phoneNumber("01012341234")
                .storeName("상점")
                .storeStatus(StoreStatus.OPEN)
                .build());
        testStoreId = savedStore.getId();

        Delivery delivery = Delivery.builder()
                .storeId(testStoreId)
                .deliveryStatus(DeliveryStatus.PREPARING)
                .order(savedOrder)
                .build();

        savedOrder.getDeliveries().add(delivery);
        Order updated = orderRepository.save(savedOrder);
        testDeliveryId = updated.getDeliveries().getFirst().getId();

        // Payment 생성: 고유한 tossOrderId와 pgTid 설정
        testTossOrderId = "toss-" + java.util.UUID.randomUUID();
        Payment payment = Payment.builder()
                .order(updated)
                .tossOrderId(testTossOrderId)
                .pgTid(testPaymentKey)
                .amount(50000)
                .cancelableAmount(50000)
                .payStatus(PaymentStatus.PAID)
                .build();

        paymentRepository.save(payment);
        paymentRepository.flush();

        Delivery verifyDelivery = deliveryRepository.findById(testDeliveryId).orElseThrow();
        Assertions.assertThat(verifyDelivery.getDeliveryStatus()).isEqualTo(DeliveryStatus.PREPARING);
    }

    @Test
    @DisplayName("사용자의 결제 취소 준비와 판매자의 배송 처리가 동시에 발생하면, 하나는 낙관적 락 예외로 실패해야 한다")
    void concurrencyTestBetweenCancelAndShip() throws InterruptedException {
        int numberOfThreads = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        AtomicBoolean cancelSuccess = new AtomicBoolean(false);
        AtomicBoolean shipSuccess = new AtomicBoolean(false);

        // 여러 스레드에서 발생하는 예외를 안전하게 수집하기 위해 스레드 안전한 큐 사용
        ConcurrentLinkedQueue<Exception> exceptions = new ConcurrentLinkedQueue<>();

        PaymentCancelRequest cancelRequest = new PaymentCancelRequest(
                testTossOrderId,  50000, testPaymentKey,"고객 단순 변심"
        );
        DeliveryShipRequest shipRequest = new DeliveryShipRequest(testOrderId, testStoreId, testDeliveryId, "TRACK-001");

        given(tossPaymentClient.requestCanceled(any(PaymentCancelRequest.class))).willReturn(
                Map.of("paymentKey", testPaymentKey)
        );

        executorService.submit(() -> {
            try {
                paymentFacade.cancelPayment(cancelRequest);
                cancelSuccess.set(true);
            } catch (Exception e) {
                exceptions.add(e);
            } finally {
                latch.countDown();
            }
        });

        executorService.submit(() -> {
            try {
                sellerDeliveryService.shipDelivery(testSellerId, shipRequest);
                shipSuccess.set(true);
            } catch (Exception e) {
                exceptions.add(e);
            } finally {
                latch.countDown();
            }
        });

        latch.await();
        executorService.shutdown();

        System.out.println("발생한 총 예외 개수: " + exceptions.size());
        for (Exception ex : exceptions) {
            System.out.println("발생한 예외: " + ex.getClass().getName() + " -> " + ex.getMessage());
        }

        boolean oneOfThemSuccess = (cancelSuccess.get() && !shipSuccess.get()) || (!cancelSuccess.get() && shipSuccess.get());
        Assertions.assertThat(oneOfThemSuccess).isTrue();

        Assertions.assertThat(exceptions).isNotEmpty();

        boolean hasValidLockException = exceptions.stream().anyMatch(ex ->
                ex instanceof org.springframework.orm.ObjectOptimisticLockingFailureException ||
                        ex instanceof jakarta.persistence.OptimisticLockException ||
                        ex instanceof com.example.i_commerce.global.exception.AppException
        );

        Assertions.assertThat(hasValidLockException).isTrue();
    }
}

