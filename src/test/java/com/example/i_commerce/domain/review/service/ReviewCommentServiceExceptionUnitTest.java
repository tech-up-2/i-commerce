package com.example.i_commerce.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.ReviewComment;
import com.example.i_commerce.domain.review.exception.ReviewErrorCode;
import com.example.i_commerce.domain.review.repository.ReviewCommentRepository;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
import com.example.i_commerce.domain.review.service.dto.CreateCommentRequest;
import com.example.i_commerce.domain.review.service.dto.UpdateCommentRequest;
import com.example.i_commerce.domain.review.validator.ReviewForbiddenWordValidator;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.exception.common.CommonErrorCode;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReviewCommentServiceExceptionUnitTest {

    @InjectMocks
    private ReviewCommentService reviewCommentService;

    @Mock
    private ReviewRepository reviewRepo;

    @Mock
    private ReviewForbiddenWordValidator reviewForbiddenWordValidator;

    @Mock
    private ReviewCommentRepository reviewCommentRepo;

    @Test
    @DisplayName("대댓글 작성 시 지정한 부모 답글이 존재하지 않으면 COMMENT_NOT_FOUND 예외가 발생한다.")
    void parentNotFound() {
        // given
        Long reviewId = 1L;
        Long userId = 3L;
        Long notFoundParentId = 999L;
        CreateCommentRequest request = new CreateCommentRequest("부모 댓글 없음", notFoundParentId);

        Review mockReview = Review.builder()
            .id(reviewId)
            .build();

        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(mockReview));
        willDoNothing().given(reviewForbiddenWordValidator).validateContent(request.getContent());

        given(reviewCommentRepo.findById(notFoundParentId)).willReturn(Optional.empty());

        // when & then
        AppException exception = assertThrows(AppException.class, () ->
            reviewCommentService.createComment(reviewId, userId, request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(ReviewErrorCode.COMMENT_NOT_FOUND);

        verify(reviewRepo, times(1)).findById(reviewId);
        verify(reviewForbiddenWordValidator, times(1)).validateContent(request.getContent());
        verify(reviewCommentRepo, times(1)).findById(notFoundParentId);

        verify(reviewCommentRepo, never()).save(any(ReviewComment.class));
    }

    @Test
    @DisplayName("대댓글 작성 시 부모 답글이 현재 리뷰에 속하지 않으면 INVALID_PERMISSION 예외가 발생한다.")
    void parentBelongsToDifferentReview() {
        // given
        Long targetReviewId = 1L;
        Long otherReviewId = 2L;
        Long userId = 3L;
        Long parentId = 10L;
        CreateCommentRequest request = new CreateCommentRequest("다른 리뷰에 시도", parentId);

        Review targetReview = Review.builder()
            .id(targetReviewId)
            .build();

        Review otherReview = Review.builder()
            .id(otherReviewId)
            .build();

        ReviewComment mockParentComment = ReviewComment.builder()
            .id(parentId)
            .review(otherReview)
            .build();

        given(reviewRepo.findById(targetReviewId)).willReturn(Optional.of(targetReview));
        willDoNothing().given(reviewForbiddenWordValidator).validateContent(request.getContent());
        given(reviewCommentRepo.findById(parentId)).willReturn(Optional.of(mockParentComment));

        // when & then
        AppException exception = assertThrows(AppException.class, () ->
            reviewCommentService.createComment(targetReviewId, userId, request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.INVALID_PERMISSION);

        verify(reviewRepo, times(1)).findById(targetReviewId);
        verify(reviewForbiddenWordValidator, times(1)).validateContent(request.getContent());
        verify(reviewCommentRepo, times(1)).findById(parentId);

        verify(reviewCommentRepo, never()).save(any(ReviewComment.class));
    }

    @Test
    @DisplayName("답글 수정 시 작성자 본인이 아니면 INVALID_PERMISSION 예외가 발생한다.")
    void invalidPermission() {
        // given
        Long commentId = 1L;
        Long requesterId = 999L;
        Long ownerId = 2L;
        UpdateCommentRequest request = new UpdateCommentRequest("수정");

        ReviewComment mockComment = ReviewComment.builder()
            .id(commentId)
            .userId(ownerId)
            .content("기존 답글 내용")
            .build();

        given(reviewCommentRepo.findById(commentId)).willReturn(Optional.of(mockComment));

        // when & then
        AppException exception = assertThrows(AppException.class, () ->
            reviewCommentService.editComment(commentId, requesterId, request)
        );

        assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.INVALID_PERMISSION);

        verify(reviewCommentRepo, times(1)).findById(commentId);
        verify(reviewForbiddenWordValidator, never()).validateContent(anyString());
    }

}
