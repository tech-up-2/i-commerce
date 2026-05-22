package com.example.i_commerce.domain.review.service;

import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.exception.ReviewErrorCode;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
import com.example.i_commerce.domain.review.service.dto.CreateReviewRequest;
import com.example.i_commerce.domain.review.service.dto.ReviewResponse;
import com.example.i_commerce.domain.review.service.dto.SearchReviewRequest;
import com.example.i_commerce.domain.review.service.dto.UpdateReviewRequest;
import com.example.i_commerce.domain.review.service.dto.ReviewListResponse;
import com.example.i_commerce.domain.review.validator.ReviewValidator;
import com.example.i_commerce.global.common.response.SliceResponse;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.exception.common.CommonErrorCode;
import com.example.i_commerce.global.s3.service.S3ImageService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepo;
    private final ReviewValidator reviewValidator;
    private final S3ImageService s3ImageService;

    @Transactional
    public Long createReview(Long orderProductId, Long userId, CreateReviewRequest dto, List<MultipartFile> imageFiles) {
        validateStarRating(dto.getStarRate());

        if (!reviewRepo.isReviewableStatus(orderProductId, userId, OrderStatus.COMPLETED)) {
            throw new AppException(ReviewErrorCode.NOT_ACTUAL_BUYER);
        }

        if (reviewRepo.existsByOrderProductIdAndUserId(orderProductId, userId)) {
            throw new AppException(ReviewErrorCode.ALREADY_REVIEWED);
        }

        reviewValidator.validateContent(dto.getContent());

        Review review = Review.from(orderProductId, userId, dto);

        Review savedReview = reviewRepo.save(review);

        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    String imageUrl = s3ImageService.uploadImage(file, "reviews");
                    review.addImage(imageUrl);
                }
            }
        }

        return savedReview.getId();
    }

    @Transactional(readOnly = true)
    public SliceResponse<ReviewResponse> searchReviews(SearchReviewRequest request, Pageable pageable) {

        Slice<Review> reviewSlice = reviewRepo.searchReviews(
            request.getOptionName(),
            request.getKeyword(),
            pageable
        );

        return SliceResponse.of(reviewSlice, ReviewResponse::from);
    }

    @Transactional(readOnly = true)
    public SliceResponse<ReviewListResponse> viewReviewList(Long productId, Pageable pageable) {

        Slice<Review> reviewSlice = reviewRepo.findSliceByProductId(productId, pageable);

        return SliceResponse.of(reviewSlice, ReviewListResponse::from);
    }

    @Transactional
    public ReviewResponse viewDetailReview(Long reviewId) {

        Review review = getReviewOrThrow(reviewId);

        ReviewResponse dto = ReviewResponse.from(review);

        return dto;
    }

    @Transactional
    public Long editReview(Long reviewId, Long userId, UpdateReviewRequest dto) {
        Review review = getReviewOrThrow(reviewId);

        validateAuthor(review, userId);
        reviewValidator.validateContent(dto.getContent());

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
        if (userId == null) {
            throw new AppException(CommonErrorCode.UNAUTHORIZED);
        }

        if (!review.getUserId().equals(userId)) {
            throw new AppException(CommonErrorCode.INVALID_PERMISSION);
        }
    }
}
