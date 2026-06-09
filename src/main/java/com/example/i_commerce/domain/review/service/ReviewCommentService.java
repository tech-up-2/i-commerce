package com.example.i_commerce.domain.review.service;

import com.example.i_commerce.domain.member.service.member.MemberService;
import com.example.i_commerce.domain.member.service.store.StoreService;
import com.example.i_commerce.domain.member.service.store.dto.StoreResponse;
import com.example.i_commerce.domain.product.application.service.ProductQueryService;
import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.ReviewComment;
import com.example.i_commerce.domain.review.exception.ReviewErrorCode;
import com.example.i_commerce.domain.review.repository.ReviewCommentRepository;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
import com.example.i_commerce.domain.review.service.dto.CreateCommentRequest;
import com.example.i_commerce.domain.review.service.dto.ReviewListResponse;
import com.example.i_commerce.domain.review.service.dto.SellerReviewManagementResponse;
import com.example.i_commerce.domain.review.service.dto.UpdateCommentRequest;
import com.example.i_commerce.domain.review.validator.ReviewForbiddenWordValidator;
import com.example.i_commerce.global.common.response.SliceResponse;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.exception.common.CommonErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewCommentService {

    private final ReviewRepository reviewRepo;
    private final ReviewCommentRepository reviewCommentRepo;
    private final ReviewForbiddenWordValidator reviewForbiddenWordValidator;
    private final StoreService storeService;
    private final ProductQueryService productQueryService;

    @Transactional
    public void createComment(Long reviewId, Long userId, CreateCommentRequest request) {

        Review review = reviewRepo.findById(reviewId)
            .orElseThrow(() -> new AppException(ReviewErrorCode.REVIEW_NOT_FOUND));

        reviewForbiddenWordValidator.validateContent(request.getContent());

        if (request.getParentId() == null) {
            ReviewComment comment = ReviewComment.of(review, userId, request);
            reviewCommentRepo.save(comment);

        } else {
            ReviewComment parent = reviewCommentRepo.findById(request.getParentId())
                .orElseThrow(() -> new AppException(ReviewErrorCode.COMMENT_NOT_FOUND));

            if (!parent.getReview().getId().equals(reviewId)) {
                throw new AppException(CommonErrorCode.INVALID_PERMISSION);
            }

            ReviewComment child = ReviewComment.ofChild(review, userId, request, parent);
            reviewCommentRepo.save(child);
        }
    }

    @Transactional
    public Long editComment(Long commentId, Long userId, UpdateCommentRequest request) {

        ReviewComment comment = reviewCommentRepo.findById(commentId)
            .orElseThrow(() -> new AppException(ReviewErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getUserId().equals(userId)) {
            throw new AppException(CommonErrorCode.INVALID_PERMISSION);
        }

        reviewForbiddenWordValidator.validateContent(request.getContent());
        comment.update(request.getContent());

        return commentId;
    }

    @Transactional(readOnly = true)
    public SliceResponse<ReviewListResponse> getSellerReviews(Long sellerId, Pageable pageable) {

        List<StoreResponse> stores = storeService.getMyStores(sellerId);

        if (stores == null || stores.isEmpty()) {
            return SliceResponse.empty();
        }

        List<Long> storeIds = stores.stream()
            .map(StoreResponse::storeId)
            .toList();

        List<Long> productIds = productQueryService.getProductIdsByStoreIds(storeIds);

        if (productIds == null || productIds.isEmpty()) {
            return SliceResponse.empty();
        }

        Slice<Review> reviews = reviewRepo.findAllByProductIdIn(productIds, pageable);

        return SliceResponse.of(reviews, ReviewListResponse::from);
    }

}
