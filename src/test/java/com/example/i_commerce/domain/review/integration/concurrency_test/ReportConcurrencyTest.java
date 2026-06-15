package com.example.i_commerce.domain.review.integration.concurrency_test;

import com.example.i_commerce.common.ReviewIntegrationTestSupport;
import com.example.i_commerce.domain.review.entity.enums.ReportType;
import com.example.i_commerce.domain.review.repository.ReviewReportRepository;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
import com.example.i_commerce.domain.review.service.ReviewReportService;
import com.example.i_commerce.domain.review.service.dto.CreateReportRequest;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.AllArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
public class ReportConcurrencyTest extends ReviewIntegrationTestSupport {

    @Autowired
    private ReviewReportService reviewReportService;

    @Autowired
    private ReviewReportRepository reviewReportRepo;

    @Autowired
    private ReviewRepository reviewRepo;

    @MockitoBean
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("1명의 유저가 동시에 한 리뷰를 5번 연타하여 신고하면, DB에는 딱 1개만 저장되어야 한다.")
    void concurrentReportBySingleUser() throws InterruptedException {
        ReviewTestSet testSet = createReviewTestEnvironment();
        Long reporterId = 99L;
        Long reviewId = testSet.review().getId();
        CreateReportRequest request = new CreateReportRequest("광고성 리뷰입니다.", ReportType.SPAM);

        int numberOfThreads = 5;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < numberOfThreads; i++) {
            executorService.submit(() -> {
                try {
                    reviewReportService.createReviewReport(reviewId, reporterId, request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                    System.out.println("신고 실패 원인 예외: " + e.getClass().getSimpleName() + " -> " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        org.assertj.core.api.Assertions.assertThat(successCount.get()).isEqualTo(1);

        org.assertj.core.api.Assertions.assertThat(failCount.get()).isEqualTo(4);
    }

    @Test
    @DisplayName("10명의 서로 다른 유저가 동시에 한 리뷰를 신고하면, 리뷰 엔티티의 @Version에 의해 낙관적 락 예외가 발생한다.")
    void concurrentReportByMultipleUsers() throws InterruptedException {
        ReviewTestSet testSet = createReviewTestEnvironment();
        Long reviewId = testSet.review().getId();
        CreateReportRequest request = new CreateReportRequest("부적절한 내용입니다.", ReportType.SPAM);

        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        ConcurrentLinkedQueue<Exception> exceptions = new ConcurrentLinkedQueue<>();
        AtomicInteger successCount = new AtomicInteger(0);

        List<BuyerOrderSet> buyers = createMultipleBuyersAndOrders(numberOfThreads, testSet.product(), testSet.productItem());

        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            long reporterId = buyers.get(index).userId();

            executorService.submit(() -> {
                try {
                    reviewReportService.createReviewReport(reviewId, reporterId, request);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    exceptions.add(e);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        System.out.println("성공한 신고 횟수: " + successCount.get());
        System.out.println("튕겨나간 예외 개수: " + exceptions.size());

        org.assertj.core.api.Assertions.assertThat(successCount.get()).isLessThan(numberOfThreads);
        org.assertj.core.api.Assertions.assertThat(exceptions).isNotEmpty();

        boolean hasValidLockException = exceptions.stream().anyMatch(ex ->
            ex instanceof org.springframework.orm.ObjectOptimisticLockingFailureException ||
                ex instanceof jakarta.persistence.OptimisticLockException
        );
        org.assertj.core.api.Assertions.assertThat(hasValidLockException).isTrue();
    }
}
