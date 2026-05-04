package com.example.i_commerce.domain.product.service;



import static com.example.i_commerce.domain.product.fixture.StockFixture.mockDeductHistory;
import static com.example.i_commerce.domain.product.fixture.StockFixture.mockOutOfStock;
import static com.example.i_commerce.domain.product.fixture.StockFixture.mockStock;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.example.i_commerce.domain.product.application.service.StockService;
import com.example.i_commerce.domain.product.facade.dto.StockDeductCommand;
import com.example.i_commerce.domain.product.entity.Stock;
import com.example.i_commerce.domain.product.entity.StockChangeType;
import com.example.i_commerce.domain.product.entity.StockHistory;
import com.example.i_commerce.domain.product.entity.StockStatus;
import com.example.i_commerce.domain.product.event.StockDepletedEvent;
import com.example.i_commerce.domain.product.repository.StockHistoryRepository;
import com.example.i_commerce.domain.product.repository.StockRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
@DisplayName("StockService 단위 테스트")
public class StockServiceTest {

    @InjectMocks
    private StockService stockService;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockHistoryRepository stockHistoryRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;


    @Nested
    @DisplayName("재고 차감")
    class DeductStocks {
        @Test
        @DisplayName("단일 상품의 재고를 차감한다.")
        void deductSingleProduct() {
            // given
            Long productItemId = 1L;
            Long orderId = 100L;
            int deductQuantity = 3;
            int initialStock = 10;

            Stock stock = mockStock(productItemId, initialStock);

            List<StockDeductCommand> commands = List.of(
                new StockDeductCommand(productItemId, deductQuantity, orderId)
            );

            given(stockRepository.findAllByProductItemIdsWithLock(anyList()))
                .willReturn(List.of(stock));

            // when
            stockService.deductStocks(commands);

            // then
            assertThat(stock.getQuantity()).isEqualTo(initialStock - deductQuantity);
            assertThat(stock.getStatus()).isEqualTo(StockStatus.IN_STOCK);
            assertThat(stock.getHistories()).hasSize(1);

            StockHistory history = stock.getHistories().getFirst();
            assertThat(history.getChangeType()).isEqualTo(StockChangeType.DEDUCT);
            assertThat(history.getChangeQuantity()).isEqualTo(deductQuantity);
            assertThat(history.getOrderId()).isEqualTo(orderId);

            then(eventPublisher).should(never()).publishEvent(any(StockDepletedEvent.class));
        }

        @Test
        @DisplayName("여러 상품의 재고를 차감한다.")
        void deductMultipleProducts() {
            // given
            Long orderId = 100L;

            Stock stock1 = mockStock(1L, 10);
            Stock stock2 = mockStock(2L, 5);
            Stock stock3 = mockStock(3L, 20);

            List<StockDeductCommand> commands = List.of(
                new StockDeductCommand(1L, 3, orderId),
                new StockDeductCommand(2L, 2, orderId),
                new StockDeductCommand(3L, 5, orderId)
            );

            given(stockRepository.findAllByProductItemIdsWithLock(anyList()))
                .willReturn(List.of(stock1, stock2, stock3));

            // when
            stockService.deductStocks(commands);

            // then
            assertThat(stock1.getQuantity()).isEqualTo(7);
            assertThat(stock2.getQuantity()).isEqualTo(3);
            assertThat(stock3.getQuantity()).isEqualTo(15);

            assertThat(stock1.getHistories()).hasSize(1);
            assertThat(stock2.getHistories()).hasSize(1);
            assertThat(stock3.getHistories()).hasSize(1);

            then(eventPublisher).should(never()).publishEvent(any(StockDepletedEvent.class));
        }

        @Test
        @DisplayName("재고가 0이 되면 재고 상태를 OUT_OF_STOCK 상태로 변경한다.")
        void changeStatusToOutOfStockWhenQuantityBecomesZero() {
            // given
            Long productItemId = 1L;
            Long orderId = 100L;
            int initialStock = 5;

            Stock stock = mockStock(productItemId, initialStock);

            List<StockDeductCommand> commands = List.of(
                new StockDeductCommand(productItemId, initialStock, orderId)
            );

            given(stockRepository.findAllByProductItemIdsWithLock(anyList()))
                .willReturn(List.of(stock));

            // when
            stockService.deductStocks(commands);

            // then
            assertThat(stock.getQuantity()).isZero();
            assertThat(stock.getStatus()).isEqualTo(StockStatus.OUT_OF_STOCK);
            assertThat(stock.isOutOfStock()).isTrue();

            ArgumentCaptor<StockDepletedEvent> eventCaptor =
                ArgumentCaptor.forClass(StockDepletedEvent.class);

            then(eventPublisher).should(times(1)).publishEvent(eventCaptor.capture());
            StockDepletedEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.productItemIds()).containsExactly(productItemId);
        }

        @Test
        @DisplayName("품절된 상품들에 대해서만 상품 상태 변경 이벤트를 발행한다.")
        void publishEventOnlyForDepletedProducts() {
            // given
            Long orderId = 100L;

            Stock stock1 = mockStock(1L, 5);
            Stock stock2 = mockStock(2L, 10);
            Stock stock3 = mockStock(3L, 3);

            List<StockDeductCommand> commands = List.of(
                new StockDeductCommand(1L, 5, orderId),
                new StockDeductCommand(2L, 3, orderId),
                new StockDeductCommand(3L, 3, orderId)
            );

            given(stockRepository.findAllByProductItemIdsWithLock(anyList()))
                .willReturn(List.of(stock1, stock2, stock3));

            // when
            stockService.deductStocks(commands);

            // then
            assertThat(stock1.getQuantity()).isZero();
            assertThat(stock1.isOutOfStock()).isTrue();

            assertThat(stock2.getQuantity()).isEqualTo(7);
            assertThat(stock2.isOutOfStock()).isFalse();

            assertThat(stock3.getQuantity()).isZero();
            assertThat(stock3.isOutOfStock()).isTrue();

            ArgumentCaptor<StockDepletedEvent> eventCaptor =
                ArgumentCaptor.forClass(StockDepletedEvent.class);

            then(eventPublisher).should(times(1)).publishEvent(eventCaptor.capture());

            StockDepletedEvent capturedEvent = eventCaptor.getValue();
            assertThat(capturedEvent.productItemIds())
                .hasSize(2)
                .containsExactlyInAnyOrder(1L, 3L);
        }

    }

    @Nested
    @DisplayName("재고 롤백")
    class RollbackStocks {

        @Test
        @DisplayName("차감된 재고를 복구한다.")
        void restoreDeductedStock() {
            // given
            Long orderId = 100L;
            Long productItemId = 1L;
            int restoredQuantity = 3;
            int currentStock = 7;

            Stock stock = mockStock(productItemId, currentStock);

            StockHistory deductHistory = mockDeductHistory(stock, restoredQuantity, orderId);

            given(stockHistoryRepository.findDeductHistoriesByOrderId(orderId))
                .willReturn(List.of(deductHistory));

            given(stockRepository.findAllByProductItemIdsWithLock(anyList()))
                .willReturn(List.of(stock));

            // when
            stockService.rollbackStocks(orderId);

            // then
            assertThat(stock.getQuantity()).isEqualTo(currentStock + restoredQuantity);
            assertThat(stock.getStatus()).isEqualTo(StockStatus.IN_STOCK);
            assertThat(stock.getHistories()).hasSize(1);

            StockHistory restoreHistory = stock.getHistories().getFirst();
            assertThat(restoreHistory.getChangeType()).isEqualTo(StockChangeType.RESTORE);
            assertThat(restoreHistory.getChangeQuantity()).isEqualTo(restoredQuantity);
            assertThat(restoreHistory.getOrderId()).isEqualTo(orderId);
        }

        @Test
        @DisplayName("여러 상품의 재고를 복구한다.")
        void rollbackMultipleProducts() {
            // given
            Long orderId = 100L;

            Stock stock1 = mockStock(1L, 7);
            Stock stock2 = mockStock(2L, 3);
            Stock stock3 = mockStock(3L, 15);

            List<StockHistory> deductHistories = List.of(
                mockDeductHistory(stock1, 3, orderId),
                mockDeductHistory(stock2, 2, orderId),
                mockDeductHistory(stock3, 5, orderId)
            );

            given(stockHistoryRepository.findDeductHistoriesByOrderId(orderId))
                .willReturn(deductHistories);

            given(stockRepository.findAllByProductItemIdsWithLock(anyList()))
                .willReturn(List.of(stock1, stock2, stock3));

            // when
            stockService.rollbackStocks(orderId);

            // then
            assertThat(stock1.getQuantity()).isEqualTo(10);
            assertThat(stock2.getQuantity()).isEqualTo(5);
            assertThat(stock3.getQuantity()).isEqualTo(20);

            assertThat(stock1.getHistories()).hasSize(1);
            assertThat(stock2.getHistories()).hasSize(1);
            assertThat(stock3.getHistories()).hasSize(1);

            stock1.getHistories().forEach(h -> {
                assertThat(h.getChangeType()).isEqualTo(StockChangeType.RESTORE);
                assertThat(h.getOrderId()).isEqualTo(orderId);
            });
        }

        @Test
        @DisplayName("재고를 복구하면 재고 상태를 IN_STOCK 상태로 변경한다.")
        void changeStatusToInStockWhenRestoringFromOutOfStock() {
            // given
            Long orderId = 100L;
            Long productItemId = 1L;
            int restoredQuantity = 5;

            Stock stock = mockOutOfStock(productItemId);
            StockHistory deductHistory = mockDeductHistory(stock, restoredQuantity, orderId);

            given(stockHistoryRepository.findDeductHistoriesByOrderId(orderId))
                .willReturn(List.of(deductHistory));

            given(stockRepository.findAllByProductItemIdsWithLock(anyList()))
                .willReturn(List.of(stock));

            // when
            stockService.rollbackStocks(orderId);

            // then
            assertThat(stock.getQuantity()).isEqualTo(restoredQuantity);
            assertThat(stock.getStatus()).isEqualTo(StockStatus.IN_STOCK);
            assertThat(stock.isOutOfStock()).isFalse();
        }

    }

}
