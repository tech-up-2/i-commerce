package com.example.i_commerce.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.i_commerce.domain.member.entity.enums.StoreStatus;
import com.example.i_commerce.domain.member.service.store.StoreService;
import com.example.i_commerce.domain.member.service.store.dto.StoreResponse;
import com.example.i_commerce.domain.product.application.service.ProductQueryService;
import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.ReviewComment;
import com.example.i_commerce.domain.review.repository.ReviewCommentRepository;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
import com.example.i_commerce.domain.review.service.dto.CreateCommentRequest;
import com.example.i_commerce.domain.review.service.dto.ReviewListResponse;
import com.example.i_commerce.domain.review.service.dto.UpdateCommentRequest;
import com.example.i_commerce.domain.review.validator.ReviewForbiddenWordValidator;
import com.example.i_commerce.global.common.response.SliceResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;

@ExtendWith(MockitoExtension.class)
public class ReviewCommentServiceUnitTest {

    @InjectMocks
    private ReviewCommentService reviewCommentService;

    @Mock
    private ReviewRepository reviewRepo;

    @Mock
    private ReviewForbiddenWordValidator reviewForbiddenWordValidator;

    @Mock
    private ReviewCommentRepository reviewCommentRepo;

    @Mock
    private StoreService storeService;

    @Mock
    private ProductQueryService productQueryService;

    @Test
    @DisplayName("사용자는 리뷰에 답글을 작성할 수 있다.")
    void createComment() {
        //given
        Long reviewId = 1L;
        Long userId = 10L;

        CreateCommentRequest request = new CreateCommentRequest("배송이 빨라요", null);

        Review mockReview = Review.builder()
            .id(reviewId)
            .build();

        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(mockReview));

        willDoNothing().given(reviewForbiddenWordValidator).validateContent(request.getContent());

        //when
        reviewCommentService.createComment(reviewId, userId, request);

        //then
        verify(reviewRepo, times(1)).findById(reviewId);

        verify(reviewCommentRepo, never()).findById(anyLong());
        verify(reviewForbiddenWordValidator, times(1)).validateContent(request.getContent());
        verify(reviewCommentRepo, times(1)).save(any(ReviewComment.class));
    }

    @Test
    @DisplayName("사용자는 자신이 작성한 답글을 수정할 수 있다.")
    void editComment() {
        // given
        Long commentId = 1L;
        Long userId = 2L;
        UpdateCommentRequest request = new UpdateCommentRequest("수정된 답글 내용입니다.");

        ReviewComment mockComment = ReviewComment.builder()
            .id(commentId)
            .userId(userId)
            .content("기존 답글 내용")
            .build();

        given(reviewCommentRepo.findById(commentId)).willReturn(Optional.of(mockComment));
        willDoNothing().given(reviewForbiddenWordValidator).validateContent(request.getContent());

        // when
        Long resultId = reviewCommentService.editComment(commentId, userId, request);

        // then
        assertThat(resultId).isEqualTo(commentId);
        assertThat(mockComment.getContent()).isEqualTo("수정된 답글 내용입니다.");

        verify(reviewCommentRepo, times(1)).findById(commentId);
        verify(reviewForbiddenWordValidator, times(1)).validateContent(request.getContent());
    }

    @Test
    @DisplayName("판매자는 본인 상점의 상품에 달린 리뷰 목록을 페이징해 확인할 수 있다.")
    void getSellerReviews_Success() {
        // given
        Long sellerId = 1L;
        Long storeId = 100L;

        PageRequest pageable = PageRequest.of(0, 10);

        StoreResponse mockStore = new StoreResponse(storeId, "상점", StoreStatus.OPEN);
        given(storeService.getMyStores(sellerId)).willReturn(List.of(mockStore));

        List<Long> productIds = List.of(200L, 201L);
        given(productQueryService.getProductIdsByStoreIds(List.of(storeId))).willReturn(productIds);

        Review mockReview1 = Review.builder().id(10L).content("상품 좋아요").build();
        Review mockReview2 = Review.builder().id(11L).content("배송 빨라요").build();
        Slice<Review> reviewSlice = new SliceImpl<>(List.of(mockReview1, mockReview2), pageable, false);

        given(reviewRepo.findAllByProductIdIn(productIds, pageable)).willReturn(reviewSlice);

        // when
        SliceResponse<ReviewListResponse> result = reviewCommentService.getSellerReviews(sellerId, pageable);

        // then
        assertThat(result.content()).hasSize(2);
        assertThat(result.content().get(0).getContent()).isEqualTo("상품 좋아요");

        verify(storeService, times(1)).getMyStores(sellerId);
        verify(productQueryService, times(1)).getProductIdsByStoreIds(List.of(storeId));
        verify(reviewRepo, times(1)).findAllByProductIdIn(productIds, pageable);
    }

    @Test
    @DisplayName("기존 답글에 대댓글을 성공적으로 작성하고 저장한다.")
    void createChildComment() {
        // given
        Long reviewId = 1L;
        Long userId = 3L;
        Long parentId = 10L;

        CreateCommentRequest request = new CreateCommentRequest("동의합니다", parentId);

        Review mockReview = Review.builder()
            .id(reviewId)
            .build();

        ReviewComment mockParentComment = ReviewComment.builder()
            .id(parentId)
            .review(mockReview)
            .build();

        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(mockReview));
        willDoNothing().given(reviewForbiddenWordValidator).validateContent(request.getContent());
        given(reviewCommentRepo.findById(parentId)).willReturn(Optional.of(mockParentComment));

        // when
        reviewCommentService.createComment(reviewId, userId, request);

        // then
        verify(reviewRepo, times(1)).findById(reviewId);
        verify(reviewForbiddenWordValidator, times(1)).validateContent(request.getContent());
        verify(reviewCommentRepo, times(1)).findById(parentId);

        verify(reviewCommentRepo, times(1)).save(any(ReviewComment.class));
    }
}
