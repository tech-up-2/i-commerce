package com.example.i_commerce.domain.review.service;

import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.ReviewLike;
import com.example.i_commerce.domain.review.exception.ReviewErrorCode;
import com.example.i_commerce.domain.review.repo.ReviewLikeRepository;
import com.example.i_commerce.domain.review.repo.ReviewRepository;
import com.example.i_commerce.global.exception.AppException;
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
    public void approveBestReview(Long reviewId) {
        Review review = getReviewOrThrow(reviewId);
        review.approveAsBest();
    }

    @Transactional
    public void cancelBestReview(Long reviewId) {
        Review review = getReviewOrThrow(reviewId);
        review.cancelBestStatus();
    }

    @Transactional
    public void excludeReviewFromBest(Long reviewId) {
        Review review = getReviewOrThrow(reviewId);
        review.excludeFromBest();
    }

    private Review getReviewOrThrow(Long reviewId) {
        return reviewRepo.findById(reviewId)
            .orElseThrow(() -> new AppException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

}
