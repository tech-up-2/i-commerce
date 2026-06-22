package com.example.i_commerce.domain.review.facade;

import com.example.i_commerce.domain.review.service.ReviewLikeService;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReviewLikeFacade {

    private final ReviewLikeService reviewLikeService;

    // 1. 재시도 로직이 필요한 좋아요 기능
    public boolean toggleLikeWithRetry(Long reviewId, Long likerId) throws InterruptedException {
        while (true) {
            try {
                return reviewLikeService.toggleLike(reviewId, likerId);
            } catch (ObjectOptimisticLockingFailureException e) {
                Thread.sleep(5);
            }
        }
    }

    // 2. 단순 전달 (판매자 권한 기능들)
    public void approveBestReview(Long reviewId, Long sellerId) {
        reviewLikeService.approveBestReview(reviewId, sellerId);
    }

    public void cancelBestReview(Long reviewId, Long sellerId) {
        reviewLikeService.cancelBestReview(reviewId, sellerId);
    }

    public void excludeFromBest(Long reviewId, Long sellerId) {
        reviewLikeService.excludeReviewFromBest(reviewId, sellerId);
    }
}