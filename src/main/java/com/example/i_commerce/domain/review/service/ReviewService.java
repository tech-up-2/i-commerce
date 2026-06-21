package com.example.i_commerce.domain.review.service;

import com.example.i_commerce.domain.member.service.store.StoreService;
import com.example.i_commerce.domain.order.entity.OrderProduct;
import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.order.service.OrderService;
import com.example.i_commerce.domain.order.service.dto.OrderProductResponse;
import com.example.i_commerce.domain.product.application.service.ProductQueryService;
import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.ReviewComment;
import com.example.i_commerce.domain.review.entity.ReviewImage;
import com.example.i_commerce.domain.review.entity.enums.ReviewStatus;
import com.example.i_commerce.domain.review.exception.ReviewErrorCode;
import com.example.i_commerce.domain.review.repository.ReviewCommentRepository;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
import com.example.i_commerce.domain.review.repository.StarRateCountProjection;
import com.example.i_commerce.domain.review.service.dto.CommentResponse;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final StoreService storeService;
    private final ProductQueryService productQueryService;
    private final ReviewCommentRepository reviewCommentRepo;

    @Transactional
    public Long createReview(Long orderProductId, Long userId, CreateReviewRequest dto, List<MultipartFile> imageFiles) {

        validateStarRating(dto.getStarRate());
        reviewForbiddenWordValidator.validateContent(dto.getContent());

        if (reviewRepo.existsByUserIdAndOrderProductIdAndStatus(userId, orderProductId, ReviewStatus.ACTIVE)) {
            throw new AppException(ReviewErrorCode.ALREADY_REVIEWED);
        }

        OrderProductResponse orderInfo = orderService.getOrderProductForReview(orderProductId);

        if (!orderInfo.userId().equals(userId)) {
            throw new AppException(ReviewErrorCode.NOT_ACTUAL_BUYER);
        }

        if (orderInfo.orderStatus() != OrderStatus.COMPLETED) {
            throw new AppException(ReviewErrorCode.REVIEW_NOT_ALLOWED_STATE);
        }

        Long productId = orderInfo.productId();

        Review review = Review.from(orderProductId, userId, productId, dto);

        if (imageFiles != null && !imageFiles.isEmpty()) {

            for (MultipartFile file : imageFiles) {
                if (!file.isEmpty()) {
                    String imageUrl = s3ImageService.uploadImage(file, "reviews");
                    review.addImage(imageUrl);
                }
            }
        }

        Review savedReview = reviewRepo.save(review);

        return savedReview.getId();
    }

    @Transactional(readOnly = true)
    public SliceResponse<ReviewResponse> searchReviews(SearchReviewRequest request, Pageable pageable) {

        Slice<Review> reviewSlice = reviewRepo.searchReviews(
            request.getProductId(),
            request.getOptionName(),
            request.getKeyword(),
            request.getStarRate(),
            pageable
        );

        return SliceResponse.of(reviewSlice, ReviewResponse::from);
    }

    @Transactional(readOnly = true)
    public SliceResponse<ReviewListResponse> viewReviewList(Long productId, Pageable pageable) {

        Slice<Review> reviewSlice = reviewRepo.findByProductIdAndStatus(productId, ReviewStatus.ACTIVE, pageable);

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

        List<ReviewComment> comments = reviewCommentRepo.findByReviewId(reviewId);

        List<CommentResponse> commentTree = assembleCommentTree(comments);

        return ReviewResponse.of(review, commentTree);
    }
    private List<CommentResponse> assembleCommentTree(List<ReviewComment> comments) {
        Map<Long, CommentResponse> map = new HashMap<>();
        List<CommentResponse> roots = new ArrayList<>();

        for (ReviewComment c : comments) {
            map.put(c.getId(), CommentResponse.from(c));
        }

        for (ReviewComment c : comments) {
            CommentResponse dto = map.get(c.getId());

            if (c.getParent() == null) {
                roots.add(dto);
            } else {
                CommentResponse parentDto = map.get(c.getParent().getId());
                if (parentDto != null) {
                    parentDto.getChildren().add(dto);
                }
            }
        }

        return roots;
    }


    @Transactional
    public Long editReview(Long reviewId, Long userId, UpdateReviewRequest dto, List<MultipartFile> newImageFiles) {
        Review review = getReviewOrThrow(reviewId);

        validateAuthor(review, userId);
        reviewForbiddenWordValidator.validateContent(dto.getContent());

        List<String> originalImageUrls = review.getImages().stream()
            .map(ReviewImage::getImageUrl)
            .toList();

        List<String> finalImageUrls = new ArrayList<>();

        if (dto.getImageUrls() == null) {
            finalImageUrls.addAll(originalImageUrls);
        } else {
            for (String requestUrl : dto.getImageUrls()) {
                if (!originalImageUrls.contains(requestUrl)) {
                    throw new AppException(ReviewErrorCode.UNMATCHED_REVIEW_IMAGE);
                }
            }
            finalImageUrls.addAll(dto.getImageUrls());
        }

        long newImageCount = 0;
        if (newImageFiles != null) {
            newImageCount = newImageFiles.stream().filter(file -> !file.isEmpty()).count();
        }

        if (finalImageUrls.size() + newImageCount > 10) {
            throw new AppException(ReviewErrorCode.EXCEED_MAX_IMAGE_COUNT);
        }

        review.getImages().stream()
            .map(ReviewImage::getImageUrl)
            .filter(oldUrl -> !finalImageUrls.contains(oldUrl))
            .forEach(url -> s3ImageService.deleteImage(url));

        if (newImageFiles != null && !newImageFiles.isEmpty()) {
            for (MultipartFile file : newImageFiles) {
                if (!file.isEmpty()) {
                    String uploadedUrl = s3ImageService.uploadImage(file, "reviews");
                    finalImageUrls.add(uploadedUrl);
                }
            }
        }

        review.update(dto.getContent(), dto.getStarRate(), finalImageUrls);

        return reviewId;
    }

    @Transactional
    public void deleteReview(Long userId,Long reviewId) {
        Review review = getReviewOrThrow(reviewId);

        validateAuthor(review, userId);

        if (review.getImages() != null && !review.getImages().isEmpty()) {
            review.getImages().forEach(image -> s3ImageService.deleteImage(image.getImageUrl()));
        }

        review.markAsDeleted();
    }

    @Transactional
    public List<ReviewListResponse> getBestReviewCandidates(Long productId, Long sellerId) {
        Long storeId = productQueryService.getStoreIdByProductId(productId);

        if (storeId == null) {
            throw new AppException(ReviewErrorCode.PRODUCT_NOT_FOUND);
        }

        if (!storeService.isStoreManager(sellerId, storeId)) {
            throw new AppException(CommonErrorCode.INVALID_PERMISSION);
        }

        List<Review> reviews = reviewRepo.findAllByProductIdAndStatus(productId, ReviewStatus.ACTIVE);

        reviews.removeIf(Review::isExcluded);

        reviews.sort((r1, r2) -> Double.compare(r2.calculateRecommendationScore(),
            r1.calculateRecommendationScore()));

        List<ReviewListResponse> responses = new ArrayList<>();
        int limit = Math.min(reviews.size(), 10);

        for (int i = 0; i < limit; i++) {
            Review review = reviews.get(i);

            responses.add(ReviewListResponse.ofCandidate(review));
        }
        return responses;
    }

    private void validateStarRating(int starRate) {
        if (starRate < 1 || starRate > 5) {
            throw new AppException(ReviewErrorCode.INVALID_STAR_RATING);
        }
    }

    private Review getReviewOrThrow(Long reviewId) {
        return reviewRepo.findByIdAndStatus(reviewId, ReviewStatus.ACTIVE)
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
