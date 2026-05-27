package com.example.i_commerce.domain.review.service;

import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.ReviewComment;
import com.example.i_commerce.domain.review.exception.ReviewErrorCode;
import com.example.i_commerce.domain.review.repository.ReviewCommentRepository;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
import com.example.i_commerce.domain.review.service.dto.CreateCommentRequest;
import com.example.i_commerce.domain.review.service.dto.SellerReviewManagementResponse;
import com.example.i_commerce.domain.review.service.dto.UpdateCommentRequest;
import com.example.i_commerce.domain.review.validator.ReviewForbiddenWordValidator;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.exception.common.CommonErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewCommentService {

    private final ReviewRepository reviewRepo;
    private final ReviewCommentRepository reviewCommentRepo;
    private final ReviewForbiddenWordValidator reviewForbiddenWordValidator;

    @Transactional
    public void createComment(Long reviewId, Long sellerId, CreateCommentRequest request) {

        Review review = reviewRepo.findById(reviewId)
            .orElseThrow(() -> new AppException(ReviewErrorCode.REVIEW_NOT_FOUND));

        if (!reviewRepo.existsByIdAndSellerId(reviewId, sellerId)) {
            throw new AppException(CommonErrorCode.INVALID_PERMISSION);
        }

        if (reviewCommentRepo.existsByReviewId(reviewId)) {
            throw new AppException(ReviewErrorCode.ALREADY_COMMENTED);
        }


        reviewForbiddenWordValidator.validateContent(request.getContent());
        ReviewComment comment = ReviewComment.of(review, sellerId, request);

        reviewCommentRepo.save(comment);

    }

    @Transactional
    public Long editComment(Long commentId, Long sellerId, UpdateCommentRequest request) {

        ReviewComment comment = reviewCommentRepo.findById(commentId)
            .orElseThrow(() -> new AppException(ReviewErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getSellerId().equals(sellerId)) {
            throw new AppException(CommonErrorCode.INVALID_PERMISSION);
        }

        reviewForbiddenWordValidator.validateContent(request.getContent());
        comment.update(request.getContent());

        return commentId;
    }

    @Transactional
    public List<SellerReviewManagementResponse> getReviewsForSeller(Long sellerId) {
        return reviewRepo.findAllBySellerId(sellerId);
    }

}
