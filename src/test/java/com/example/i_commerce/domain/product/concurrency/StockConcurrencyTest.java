package com.example.i_commerce.domain.product.concurrency;


import static org.assertj.core.api.Assertions.assertThat;

import com.example.i_commerce.common.ProductIntegrationTestSupport;
import com.example.i_commerce.domain.product.application.dto.StockDeductCommand;
import com.example.i_commerce.domain.product.application.service.StockService;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.Stock;
import com.example.i_commerce.domain.product.entity.StockHistory;
import com.example.i_commerce.domain.product.entity.enums.StockChangeType;
import com.example.i_commerce.domain.product.entity.enums.StockStatus;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.fixture.CategoryFixture;
import com.example.i_commerce.domain.product.fixture.ProductFixture;
import com.example.i_commerce.domain.product.fixture.ProductItemFixture;
import com.example.i_commerce.global.exception.AppException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest
public class StockConcurrencyTest extends ProductIntegrationTestSupport {

    @Autowired
    private StockService stockService;

    private ProductItem createItem(String sku, int stockQuantity) {
        Category category = categoryRepository.save(CategoryFixture.rootCategory());
        Product product = ProductFixture.createProductWithCategory(category);
        ProductItem item = ProductItemFixture.createItemWithStock(
            product, sku, stockQuantity
        );
        productRepository.save(product);
        return item;
    }

    private StockDeductCommand createDeductCommand(Long itemId, int quantity, Long orderId) {
        return StockDeductCommand.builder()
            .productItemId(itemId)
            .quantity(quantity)
            .orderId(orderId)
            .build();
    }

    @Test
    @DisplayName("재고 범위 내 동시 차감 요청이 모두 성공한다.")
    void deductStock_success_withinStockLimit() throws Exception {
        // given
        int initialStock = 1000;
        int threadCount = 100;
        int deductAmount = 1;
        int totalDeductAmount = threadCount * deductAmount;
        int expectedRemainingStock = initialStock - totalDeductAmount;
        Long itemId = createItem("SKU", initialStock).getId();

        // when
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<Exception> thrownExceptions = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final long orderId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    stockService.deductStocks(List.of(
                        createDeductCommand(itemId, deductAmount, orderId)
                    ));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    thrownExceptions.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // then
        assertThat(thrownExceptions).isEmpty();

        Stock finalStock = stockRepository.findByProductItemId(itemId).orElseThrow();
        assertThat(finalStock.getQuantity()).isEqualTo(expectedRemainingStock);
        assertThat(finalStock.getStatus()).isEqualTo(StockStatus.IN_STOCK);

        List<StockHistory> histories =
            stockHistoryRepository.findAllByProductItemId(itemId);
        assertThat(histories).hasSize(threadCount);

    }

    @Test
    @DisplayName("재고를 초과하는 동시 차감 요청이 들어올 시 범위 내 요청만 성공한다.")
    void deductStock_success_withoutLimit_onlyWithin() throws Exception {
        // given
        int initialStock = 5;
        int threadCount = 10;
        int deductAmount = 1;
        int expectedSuccessCount = initialStock / deductAmount;
        int expectedFailCount = threadCount - expectedSuccessCount;
        int expectedRemainingStock = 0;
        ProductItem item = createItem("SKU", initialStock);

        // when
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<Exception> thrownExceptions = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final long orderId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    stockService.deductStocks(List.of(
                        createDeductCommand(item.getId(), deductAmount, orderId)
                    ));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    thrownExceptions.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        // then
        assertThat(thrownExceptions).hasSize(expectedFailCount);

        assertThat(thrownExceptions)
            .allMatch(e -> e instanceof AppException &&
                ((AppException) e).getErrorCode() == ProductErrorCode.INSUFFICIENT_STOCK);

        Stock finalStock = stockRepository.findByProductItemId(item.getId()).orElseThrow();
        assertThat(finalStock.getQuantity()).isEqualTo(expectedRemainingStock);
        assertThat(finalStock.getStatus()).isEqualTo(StockStatus.OUT_OF_STOCK);

        List<StockHistory> histories =
            stockHistoryRepository.findAllByProductItemId(item.getId());

        assertThat(histories).hasSize(expectedSuccessCount);
    }
    
    @Test
    @DisplayName("동일한 상품에 재고 차감 요청과 복구 요청이 동시에 들어와도 정상적으로 동작한다.")
    void deductAndRestore_allSuccess() throws Exception {
        // given
        AtomicLong orderIDGenerator = new AtomicLong(1L);
        int initialStock = 50;
        int deductThreadCount = 10;
        int deductAmount = 1;
        int totalDeductAmount = deductThreadCount * deductAmount;

        int restoreThreadCount = 5;
        List<Long> preDeductedOrderIds = IntStream.range(0, restoreThreadCount)
            .mapToLong(i -> orderIDGenerator.getAndIncrement())
            .boxed()
            .toList();
        int restoreAmount = 1;
        int totalRestoreAmount = restoreThreadCount * restoreAmount;

        int expectedRemainingStock = initialStock
            - totalRestoreAmount
            - totalDeductAmount
            + totalRestoreAmount;

        ProductItem item = createItem("SKU-DEDUCT-RESTORE", initialStock);

        for (Long orderId : preDeductedOrderIds) {
            stockService.deductStocks(List.of(
                createDeductCommand(item.getId(), restoreAmount, orderId)
            ));
        }

        // when
        int totalThreadCount = deductThreadCount + restoreThreadCount;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(totalThreadCount);
        List<Exception> thrownExceptions = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(totalThreadCount);

        for (int i = 0; i < deductThreadCount; i++) {
            final long orderId = orderIDGenerator.getAndIncrement();
            executor.submit(() -> {
                try {
                    startLatch.await();
                    stockService.deductStocks(List.of(
                        createDeductCommand(item.getId(), deductAmount, orderId)
                    ));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    thrownExceptions.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        for (Long orderId : preDeductedOrderIds) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    stockService.rollbackStocks(orderId);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    thrownExceptions.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();
    
        // then
        assertThat(thrownExceptions).isEmpty();

        Stock finalStock = stockRepository.findByProductItemId(item.getId()).orElseThrow();
        assertThat(finalStock.getQuantity()).isEqualTo(expectedRemainingStock);
        assertThat(finalStock.getStatus()).isEqualTo(StockStatus.IN_STOCK);

        List<StockHistory> allHistories =
            stockHistoryRepository.findAllByProductItemId(item.getId());

        int expectedTotalHistoryCount =
            restoreThreadCount + deductThreadCount + restoreThreadCount;
        assertThat(allHistories).hasSize(expectedTotalHistoryCount);

        long deductHistoryCount = allHistories.stream()
            .filter(h -> h.getChangeType() == StockChangeType.DEDUCT)
            .count();
        assertThat(deductHistoryCount).isEqualTo(restoreThreadCount + deductThreadCount);

        long restoreHistoryCount = allHistories.stream()
            .filter(h -> h.getChangeType() == StockChangeType.RESTORE)
            .count();
        assertThat(restoreHistoryCount).isEqualTo(restoreThreadCount);

    }

    @Test
    @DisplayName("동일한 주문 ID의 복구 요청이 동시에 들어오면 하나의 요청만 성공한다.")
    void rollbackStocks_sameOrderId_onlyOneSuccess() throws Exception {
        // given
        AtomicLong orderIDGenerator = new AtomicLong(1L);
        int initialStock = 50;
        int deductAmount = 5;
        int threadCount = 5;

        long targetOrderId = orderIDGenerator.getAndIncrement();
        ProductItem item = createItem("SKU-RESTORE-001", initialStock);
        stockService.deductStocks(List.of(
            createDeductCommand(item.getId(), deductAmount, targetOrderId)
        ));
        int expectedRemainingStock = initialStock - deductAmount + deductAmount;

        // when
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<Exception> thrownExceptions = Collections.synchronizedList(new ArrayList<>());

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    stockService.rollbackStocks(targetOrderId); // 동일한 orderId
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    thrownExceptions.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();
    
        // then
        assertThat(thrownExceptions)
            .hasSize(threadCount - 1);

        assertThat(thrownExceptions)
            .allMatch(e -> e instanceof AppException &&
                ((AppException) e).getErrorCode() == ProductErrorCode.STOCK_ALREADY_RESTORED);

        Stock finalStock = stockRepository.findByProductItemId(item.getId()).orElseThrow();
        assertThat(finalStock.getQuantity()).isEqualTo(expectedRemainingStock);
        assertThat(finalStock.getStatus()).isEqualTo(StockStatus.IN_STOCK);

        List<StockHistory> allHistories =
            stockHistoryRepository.findAllByProductItemId(item.getId());

        assertThat(allHistories).hasSize(2);

        long restoreHistoryCount = allHistories.stream()
            .filter(h -> h.getChangeType() == StockChangeType.RESTORE)
            .count();
        assertThat(restoreHistoryCount).isEqualTo(1);
    }

    @Test
    @DisplayName("ID가 역순으로 요청되어도 락 획득이 데드락 없이 정상적으로 성공한다.")
    void deductStocks_success_multipleProducts_noDeadlock() throws Exception {
        // given
        AtomicLong orderIDGenerator = new AtomicLong(1L);
        int initialStock = 50;
        int threadCount = 10;
        int deductAmountPerThread = 1;
        int totalDeductAmount = threadCount * deductAmountPerThread;
        int expectedRemainingStock = initialStock - totalDeductAmount;

        ProductItem itemA = createItem("SKU-MULTI-A", initialStock);
        ProductItem itemB = createItem("SKU-MULTI-B", initialStock);

        Long lowerItemId  = Math.min(itemA.getId(), itemB.getId());
        Long higherItemId = Math.max(itemA.getId(), itemB.getId());

        // when
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        List<Exception> thrownExceptions = Collections.synchronizedList(new ArrayList<>());
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            final long orderId = orderIDGenerator.getAndIncrement();
            final boolean isEvenThread = (i % 2 == 0);

            executor.submit(() -> {
                try {
                    startLatch.await();
                    List<StockDeductCommand> commands = isEvenThread
                        ? List.of(
                        createDeductCommand(lowerItemId, deductAmountPerThread, orderId),
                        createDeductCommand(higherItemId, deductAmountPerThread, orderId)
                    ) : List.of(
                        createDeductCommand(higherItemId, deductAmountPerThread, orderId),
                        createDeductCommand(lowerItemId, deductAmountPerThread, orderId)
                    );
                    stockService.deductStocks(commands);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    thrownExceptions.add(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        doneLatch.await();
        executor.shutdown();

        assertThat(thrownExceptions).isEmpty();

        Stock finalStockA = stockRepository.findByProductItemId(itemA.getId()).orElseThrow();
        Stock finalStockB = stockRepository.findByProductItemId(itemB.getId()).orElseThrow();

        assertThat(finalStockA.getQuantity()).isEqualTo(expectedRemainingStock);
        assertThat(finalStockB.getQuantity()).isEqualTo(expectedRemainingStock);

        List<StockHistory> historiesA =
            stockHistoryRepository.findAllByProductItemId(itemA.getId());
        List<StockHistory> historiesB =
            stockHistoryRepository.findAllByProductItemId(itemB.getId());

        assertThat(historiesA).hasSize(threadCount);
        assertThat(historiesB).hasSize(threadCount);
    }


}