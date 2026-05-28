package com.example.i_commerce.domain.review.service;

import com.example.i_commerce.domain.order.entity.OrderProduct;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.order.service.OrderService;
import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.exception.ReviewErrorCode;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
import com.example.i_commerce.domain.review.repository.StarRateCountProjection;
import com.example.i_commerce.domain.review.service.dto.CreateReviewRequest;
import com.example.i_commerce.domain.review.service.dto.ReviewResponse;
import com.example.i_commerce.domain.review.service.dto.ReviewStatsResponse;
import com.example.i_commerce.domain.review.service.dto.SearchReviewRequest;
import com.example.i_commerce.domain.review.service.dto.UpdateReviewRequest;
import com.example.i_commerce.domain.review.service.dto.ReviewListResponse;
import com.example.i_commerce.domain.review.validator.ReviewForbiddenWordValidator;
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
    private final ReviewForbiddenWordValidator reviewForbiddenWordValidator;
    private final S3ImageService s3ImageService;
    private final OrderService orderService;

    @Transactional
    public Long createReview(Long orderProductId, Long userId, CreateReviewRequest dto, List<MultipartFile> imageFiles) {

        validateStarRating(dto.getStarRate());
        reviewForbiddenWordValidator.validateContent(dto.getContent());

        if (reviewRepo.existsByOrderProductId(orderProductId)) {
            throw new AppException(ReviewErrorCode.ALREADY_REVIEWED);
        }

        OrderProduct orderProduct = orderService.findOrderProductById(orderProductId);

        if (!orderProduct.getOrder().getUserId().equals(userId)) {
            throw new AppException(ReviewErrorCode.NOT_ACTUAL_BUYER);
        }

        if (orderProduct.getOrder().getOrderStatus() != OrderStatus.COMPLETED) {
            throw new AppException(ReviewErrorCode.REVIEW_NOT_ALLOWED_STATE);
        }

        Long productId = orderProduct.getProductSkuId();

        Review review = Review.from(orderProductId, userId, productId, dto);
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
            request.getStarRate(),
            pageable
        );

        return SliceResponse.of(reviewSlice, ReviewResponse::from);
    }

    @Transactional(readOnly = true)
    public SliceResponse<ReviewListResponse> viewReviewList(Long productId, Pageable pageable) {

        Slice<Review> reviewSlice = reviewRepo.findByProductId(productId, pageable);

        return SliceResponse.of(reviewSlice, ReviewListResponse::from);
    }

    @Transactional(readOnly = true)
    public ReviewStatsResponse getProductReviewStats(Long productId) {

        List<StarRateCountProjection> projections = reviewRepo.getStarRateStats(productId);

        long totalReviewCount = 0;
        long totalScore = 0;

        java.util.Map<Integer, Long> starCountMap = new java.util.HashMap<>();
        for (int i = 1; i <= 5; i++) {
            starCountMap.put(i, 0L);
        }

        for (StarRateCountProjection proj : projections) {
            long count = proj.getCount();
            int star = proj.getStarRate();

            totalReviewCount += count;
            totalScore += (count * star);

            starCountMap.put(star, count);
        }

        List<ReviewStatsResponse.StarRateDetail> starDetails = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            starDetails.add(new ReviewStatsResponse.StarRateDetail(i, starCountMap.get(i)));
        }

        double averageStarRate = 0.0;
        if (totalReviewCount > 0) {
            averageStarRate = Math.round((double) totalScore / totalReviewCount * 10) / 10.0;
        }

        return ReviewStatsResponse.builder()
            .starDetails(starDetails)
            .averageStarRate(averageStarRate)
            .totalReviewCount(totalReviewCount)
            .build();
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
        reviewForbiddenWordValidator.validateContent(dto.getContent());

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
    public List<ReviewListResponse> getBestReviewCandidates(Long productId) {

        List<Review> reviews = reviewRepo.findAllByProductIdAndDeletedAtIsNull(productId);

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
