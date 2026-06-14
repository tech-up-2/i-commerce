package com.example.i_commerce.domain.review.integration.concurrency_test;

import com.example.i_commerce.common.ReviewIntegrationTestSupport;
import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.facade.ReviewLikeFacade;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ReviewLikeConcurrencyTest extends ReviewIntegrationTestSupport {
    @Autowired
    private ReviewLikeFacade reviewLikeFacade;

    @Autowired
    private ReviewRepository reviewRepository;

    @Test
    @DisplayName("10명의 서로 다른 유저가 동시에 한 리뷰의 '좋아요'를 누르면, Facade의 재시도 로직으로 누락 없이 10개 모두 성공해야 한다.")
    void concurrentReviewLikeWithFacade() throws InterruptedException {
        ReviewTestSet testSet = createReviewTestEnvironment();
        Long reviewId = testSet.review().getId();

        int numberOfThreads = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(numberOfThreads);

        List<BuyerOrderSet> buyers = createMultipleBuyersAndOrders(numberOfThreads, testSet.product(), testSet.productItem());

        for (int i = 0; i < numberOfThreads; i++) {
            final int index = i;
            executorService.submit(() -> {
                try {
                    reviewLikeFacade.toggleLikeWithRetry(reviewId, buyers.get(index).userId());
                } catch (Exception e) {
                    System.out.println("파사드가 막지 못한 에러: " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executorService.shutdown();

        Review finalReview = reviewRepository.findById(reviewId).orElseThrow();
        System.out.println("최종 리뷰의 좋아요 개수: " + finalReview.getLikeCount());

        org.assertj.core.api.Assertions.assertThat(finalReview.getLikeCount()).isEqualTo(10L);
    }
}
