package com.example.i_commerce.domain.review.service;

import com.example.i_commerce.domain.member.service.store.StoreService;
import com.example.i_commerce.domain.product.application.service.ProductQueryService;
import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.ReviewLike;
import com.example.i_commerce.domain.review.exception.ReviewErrorCode;
import com.example.i_commerce.domain.review.repository.ReviewLikeRepository;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.exception.common.CommonErrorCode;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewLikeService {

    private final ReviewRepository reviewRepo;
    private final ReviewLikeRepository reviewLikeRepo;
    private final ProductQueryService productQueryService;
    private final StoreService storeService;
    private static final double BEST_THRESHOLD = 80.0;

    @Transactional
    public boolean toggleLike(Long reviewId, Long likerId) {

        Review review = getReviewOrThrow(reviewId);

        Optional<ReviewLike> existingLike = reviewLikeRepo.findByReviewAndLikerId(review, likerId);

        boolean isLiked;

        if (existingLike.isPresent()) {
            reviewLikeRepo.delete(existingLike.get());
            review.decreaseLikeCount();
            isLiked = false;
        } else {
            ReviewLike newLike = ReviewLike.builder()
                .review(review)
                .likerId(likerId)
                .build();
            reviewLikeRepo.save(newLike);
            review.increaseLikeCount();
            isLiked = true;
        }

        review.checkBestEligibility(BEST_THRESHOLD);
        return isLiked;

    }

    @Transactional
    public void approveBestReview(Long reviewId, Long sellerId) {
        Review review = getReviewOrThrow(reviewId);
        validateStoreManager(review, sellerId);
        review.approveAsBest();
    }

    @Transactional
    public void cancelBestReview(Long reviewId, Long sellerId) {
        Review review = getReviewOrThrow(reviewId);
        validateStoreManager(review, sellerId);
        review.cancelBestStatus();
    }

    @Transactional
    public void excludeReviewFromBest(Long reviewId, Long sellerId) {
        Review review = getReviewOrThrow(reviewId);
        validateStoreManager(review, sellerId);
        review.excludeFromBest();
    }

    private Review getReviewOrThrow(Long reviewId) {
        return reviewRepo.findById(reviewId)
            .orElseThrow(() -> new AppException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

    private void validateStoreManager(Review review, Long sellerId) {
        Long storeId = productQueryService.getStoreIdByProductId(review.getProductId());

        if (!storeService.isStoreManager(sellerId, storeId)) {
            throw new AppException(CommonErrorCode.INVALID_PERMISSION);
        }
    }

}
