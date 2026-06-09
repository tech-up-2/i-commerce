package com.example.i_commerce.domain.product.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.Stock;
import com.example.i_commerce.domain.product.entity.StockHistory;
import com.example.i_commerce.domain.product.entity.enums.StockChangeType;
import com.example.i_commerce.domain.product.entity.enums.StockStatus;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.global.exception.AppException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class StockTest {
    private ProductItem productItem;

    @BeforeEach
    void setUp() {
        productItem = mock(ProductItem.class);
    }

    @Nested
    @DisplayName("재고 생성 테스트")
    class CreateStockTest {

        @Test
        @DisplayName("정상적으로 재고를 생성한다.")
        void of_success() {
            // when
            Stock stock = Stock.of(productItem, 10);

            // then
            assertThat(stock.getQuantity()).isEqualTo(10);
            assertThat(stock.getProductItem()).isEqualTo(productItem);
            assertThat(stock.getStatus()).isEqualTo(StockStatus.IN_STOCK);
        }

        @Test
        @DisplayName("음수 수량으로 재고 생성 요청시 예외가 발생한다.")
        void of_fail_negativeQuantityNotAllowed() {
            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                Stock.of(productItem, -1));
            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.NEGATIVE_QUANTITY_NOT_ALLOWED);
        }

    }

    @Nested
    @DisplayName("재고 차감 테스트")
    class DeductTest {

        @Test
        @DisplayName("재고를 정상적으로 차감한다.")
        void deduct_success() {
            // given
            Stock stock = Stock.of(productItem, 10);

            // when
            stock.deduct(3, 100L);

            // then
            assertThat(stock.getQuantity()).isEqualTo(7);
            assertThat(stock.getStatus()).isEqualTo(StockStatus.IN_STOCK);
            assertThat(stock.isOutOfStock()).isFalse();
        }

        @Test
        @DisplayName("재고가 전부 차감되면 OUT_OF_STOCK 상태로 변경된다.")
        void deduct_success_becomesOutOfStock() {
            // given
            Stock stock = Stock.of(productItem, 5);

            // when
            stock.deduct(5, 100L);

            // then
            assertThat(stock.getQuantity()).isEqualTo(0);
            assertThat(stock.getStatus()).isEqualTo(StockStatus.OUT_OF_STOCK);
            assertThat(stock.isOutOfStock()).isTrue();
        }

        @Test
        @DisplayName("재고보다 많은 수량 차감 요청이 들어오면 예외가 발생한다.")
        void deduct_fail_whenInsufficientStock() {
            // given
            Stock stock = Stock.of(productItem, 3);

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                stock.deduct(5, 100L));
            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.INSUFFICIENT_STOCK);
        }

        @Test
        @DisplayName("UNAVAILABLE 상태에서 차감이 요청될시 예외가 발생한다.")
        void deduct_fail_throwsException_whenUnavailable() {
            // given
            Stock stock = Stock.of(productItem, 10);
            stock.markUnavailable();
            assertThat(stock.getStatus()).isEqualTo(StockStatus.UNAVAILABLE);

            // when & then
            AppException exception = assertThrows(AppException.class, () ->
                stock.deduct(5, 100L));
            assertThat(exception.getErrorCode())
                .isEqualTo(ProductErrorCode.STOCK_UNAVAILABLE);
        }

        @Test
        @DisplayName("재고 차감 시 DEDUCT 타입의 StockHistory가 생성된다.")
        void deduct_success_recordsDeductHistory() {
            // given
            Stock stock = Stock.of(productItem, 10);
            Long orderId = 100L;
            int amount = 3;

            // when
            stock.deduct(amount, orderId);

            // then
            assertThat(stock.getHistories()).hasSize(1);

            StockHistory history = stock.getHistories().getFirst();
            assertThat(history.getChangeType()).isEqualTo(StockChangeType.DEDUCT);
            assertThat(history.getChangeQuantity()).isEqualTo(amount);
            assertThat(history.getOrderId()).isEqualTo(orderId);
            assertThat(history.getStock()).isEqualTo(stock);
        }

        @Test
        @DisplayName("재고 차감시 예외가 발생하면 수량이 변하지 않는다.")
        void deduct_success_quantityUnchanged() {
            // given
            Stock stock = Stock.of(productItem, 3);

            // when
            assertThatThrownBy(() -> stock.deduct(5, 100L))
                .isInstanceOf(AppException.class);

            // then
            assertThat(stock.getQuantity()).isEqualTo(3);
            assertThat(stock.getStatus()).isEqualTo(StockStatus.IN_STOCK);
        }

        @Test
        @DisplayName("재고 차감시 예외가 발생하면 StockHistory가 생성되지 않는다.")
        void deduct_success_notRecordHistory() {
            // given
            Stock stock = Stock.of(productItem, 3);

            // when
            assertThatThrownBy(() -> stock.deduct(5, 100L))
                .isInstanceOf(AppException.class);

            // then
            assertThat(stock.getHistories()).isEmpty();
        }

        @Test
        @DisplayName("StockHistory가 순서대로 기록된다.")
        void deduct_success_recordsHistoriesInOrder() {
            // given
            Stock stock = Stock.of(productItem, 10);

            // when
            stock.deduct(3, 100L);
            stock.deduct(2, 200L);

            // then
            assertThat(stock.getHistories()).hasSize(2);
            assertThat(stock.getHistories())
                .extracting(StockHistory::getOrderId)
                .containsExactly(100L, 200L);
        }

    }

    @Nested
    @DisplayName("재고 복구 테스트")
    class RestoreTest {

        @Test
        @DisplayName("재고를 정상적으로 복구한다.")
        void restore_success() {
            // given
            Stock stock = Stock.of(productItem, 10);
            stock.deduct(3, 100L);
            assertThat(stock.getQuantity()).isEqualTo(7);

            // when
            stock.restore(3, 100L);

            // then
            assertThat(stock.getQuantity()).isEqualTo(10);
            assertThat(stock.getStatus()).isEqualTo(StockStatus.IN_STOCK);
        }

        @Test
        @DisplayName("OUT_OF_STOCK 상태에서 복구하면 IN_STOCK 상태로 변경된다.")
        void restore_success_changesStatus_fromOutOfStockToInStock() {
            // given
            Stock stock = Stock.of(productItem, 5);
            stock.deduct(5, 100L);
            assertThat(stock.isOutOfStock()).isTrue();

            // when
            stock.restore(5, 100L);

            // then
            assertThat(stock.getStatus()).isEqualTo(StockStatus.IN_STOCK);
            assertThat(stock.isOutOfStock()).isFalse();
        }

        @Test
        @DisplayName("UNAVAILABLE 상태에서 복구하면 IN_STOCK 상태로 변경된다.")
        void restore_success_changesStatus_fromUnavailableToInStock() {
            // given
            Stock stock = Stock.of(productItem, 5);
            stock.markUnavailable();
            assertThat(stock.getStatus()).isEqualTo(StockStatus.UNAVAILABLE);

            // when
            stock.restore(3, 100L);

            // then
            assertThat(stock.getStatus()).isEqualTo(StockStatus.IN_STOCK);
            assertThat(stock.isOutOfStock()).isFalse();
        }

        @Test
        @DisplayName("재고 복구시 RESTORE 타입의 StockHistory가 생성된다.")
        void restore_recordsHistory() {
            // given
            Stock stock = Stock.of(productItem, 10);
            stock.deduct(3, 100L);
            Long orderId = 100L;
            int amount = 3;

            // when
            stock.restore(amount, orderId);

            // then
            assertThat(stock.getHistories()).hasSize(2);
            StockHistory restoreHistory = stock.getHistories().getLast();
            assertThat(restoreHistory.getChangeType()).isEqualTo(StockChangeType.RESTORE);
            assertThat(restoreHistory.getChangeQuantity()).isEqualTo(amount);
            assertThat(restoreHistory.getOrderId()).isEqualTo(orderId);
            assertThat(restoreHistory.getStock()).isEqualTo(stock);
        }

    }

    @Nested
    @DisplayName("재고 비활성화 테스트")
    class MarkUnavailableTest {

        @Test
        @DisplayName("IN_STOCK 상태에서 비활성화하면 UNAVAILABLE 상태로 변경된다.")
        void markUnavailable_success_whenInStock() {
            // given
            Stock stock = Stock.of(productItem, 10);
            assertThat(stock.getStatus()).isEqualTo(StockStatus.IN_STOCK);

            // when
            stock.markUnavailable();

            // then
            assertThat(stock.getStatus()).isEqualTo(StockStatus.UNAVAILABLE);
        }

        @Test
        @DisplayName("UNAVAILABLE 상태에서 비활성화하면 상태가 변하지 않는다.")
        void markUnavailable_success_notChanged_whenUnavailable() {
            // given
            Stock stock = Stock.of(productItem, 10);
            stock.markUnavailable();
            assertThat(stock.getStatus()).isEqualTo(StockStatus.UNAVAILABLE);

            // when
            stock.markUnavailable();

            // then
            assertThat(stock.getStatus()).isEqualTo(StockStatus.UNAVAILABLE);
        }

        @Test
        @DisplayName("OUT_OF_STOCK 상태에서 비활성화하면 UNAVAILABLE 상태로 변경된다.")
        void markUnavailable_success_whenOutOfStock() {
            // given
            Stock stock = Stock.of(productItem, 5);
            stock.deduct(5, 100L);
            assertThat(stock.isOutOfStock()).isTrue();

            // when
            stock.markUnavailable();

            // then
            assertThat(stock.getStatus()).isEqualTo(StockStatus.UNAVAILABLE);
        }

    }

    @Nested
    @DisplayName("품절 상태 확인 테스트")
    class IsOutOfStockTest {

        @Test
        @DisplayName("IN_STOCK 상태면 false를 반환한다.")
        void isOutOfStock_success_returnsFalse_whenInStock() {
            // given
            Stock stock = Stock.of(productItem, 10);

            // when & then
            assertThat(stock.isOutOfStock()).isFalse();
        }

        @Test
        @DisplayName("OUT_OF_STOCK 상태면 true를 반환한다.")
        void isOutOfStock_success_returnsTrue_whenOutOfStock() {
            // given
            Stock stock = Stock.of(productItem, 5);
            stock.deduct(5, 100L);

            // when & then
            assertThat(stock.isOutOfStock()).isTrue();
        }

        @Test
        @DisplayName("UNAVAILABLE 상태면 false를 반환한다.")
        void isOutOfStock_success_returnsFalse_whenUnavailable() {
            // given
            Stock stock = Stock.of(productItem, 10);
            stock.markUnavailable();

            // when & then
            assertThat(stock.isOutOfStock()).isFalse();
        }

    }

}
