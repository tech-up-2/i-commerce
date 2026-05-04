package com.example.i_commerce.domain.review.service;

import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.exception.ReviewErrorCode;
import com.example.i_commerce.domain.review.repo.ReviewRepository;
import com.example.i_commerce.domain.review.service.dto.CreateReviewRequest;
import com.example.i_commerce.domain.review.service.dto.ReviewResponse;
import com.example.i_commerce.domain.review.service.dto.UpdateReviewRequest;
import com.example.i_commerce.domain.review.service.dto.ReviewListResponse;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.exception.common.CommonErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepo;

    @Transactional
    public Long createReview(CreateReviewRequest dto) {
        validateStarRating(dto.getStarRate());

        if (reviewRepo.existsByOrderProductIdAndUserId(dto.getOrderProductId(), dto.getUserId())) {
            throw new AppException(ReviewErrorCode.ALREADY_REVIEWED);
        }

        Review review = Review.from(dto);

        Review savedReview = reviewRepo.save(review);

        return savedReview.getId();
    }

    @Transactional
    public List<ReviewListResponse> viewReviewList(Long orderProductId) {
        List<Review> reviews = reviewRepo.findAllByOrderProductIdAndDeletedAtIsNull(orderProductId);

        List<ReviewListResponse> responseDtoLists = new ArrayList<>();

        for (Review review : reviews) {
            ReviewListResponse responses = ReviewListResponse.from(review);
            responseDtoLists.add(responses);
        }
        return responseDtoLists;
    }

    @Transactional
    public ReviewResponse viewDetailReview(Long reviewId) {

        Review review = getReviewOrThrow(reviewId);

        ReviewResponse dto = ReviewResponse.from(review);

        return dto;
    }

    @Transactional
    public Long editReview(Long reviewId, UpdateReviewRequest dto) {
        Review review = getReviewOrThrow(reviewId);

        validateAuthor(review, dto.getUserId());

        review.update(dto.getContent(), dto.getStarRate(), dto.getImageUrls());

        return reviewId;
    }

    @Transactional
    public void deleteReview(Long userId,Long reviewId) {
        Review review = getReviewOrThrow(reviewId);

        validateAuthor(review, userId);

        review.delete();
    }

    @Transactional
    public List<ReviewListResponse> getBestReviewCandidates(Long orderProductId) {

        List<Review> reviews = reviewRepo.findAllByOrderProductIdAndDeletedAtIsNull(orderProductId);

        for (Review r : reviews) {
            r.updateBestStatus(false);
        }

        reviews.sort((r1, r2) -> Double.compare(r2.calculateRecommendationScore(),
            r1.calculateRecommendationScore()));

        List<ReviewListResponse> responses = new ArrayList<>();
        int limit = Math.min(reviews.size(), 10);

        for (int i = 0; i < limit; i++) {
            Review review = reviews.get(i);

            review.updateBestStatus(true);

            responses.add(ReviewListResponse.from(review));

        }
        return responses;
    }

    private void validateStarRating(int starRate) {
        if (starRate < 1 || starRate > 5) {
            throw new AppException(ReviewErrorCode.INVALID_STAR_RATING);
        }
    }

    private Review getReviewOrThrow(Long reviewId) {
        return reviewRepo.findById(reviewId)
        .orElseThrow(() -> new AppException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

    private void validateAuthor(Review review, Long userId) {
        if (!review.getUserId().equals(userId)) {
            throw(new AppException(CommonErrorCode.INVALID_PERMISSION));
        }
    }
}
