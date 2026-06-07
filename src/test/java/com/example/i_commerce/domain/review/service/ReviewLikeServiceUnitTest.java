package com.example.i_commerce.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.i_commerce.domain.member.service.store.StoreService;
import com.example.i_commerce.domain.product.application.service.ProductQueryService;
import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.ReviewLike;
import com.example.i_commerce.domain.review.entity.enums.ReviewIsBestStatus;
import com.example.i_commerce.domain.review.repository.ReviewLikeRepository;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReviewLikeServiceUnitTest {

    @InjectMocks
    private ReviewLikeService reviewLikeService;

    @Mock
    private ReviewRepository reviewRepo;

    @Mock
    private ReviewLikeRepository reviewLikeRepo;

    @Mock
    private ProductQueryService productQueryService;

    @Mock
    private StoreService storeService;

    @Test
    @DisplayName("좋아요를 누르면 추가된다.")
    void toggleLike_add() {
        //given
        Long reviewId = 1L;
        Long likerId = 10L;

        Review mockReview = Review.builder()
            .id(reviewId)
            .likeCount(0L)
            .build();

        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(mockReview));
        given(reviewLikeRepo.findByReviewAndLikerId(mockReview, likerId)).willReturn(Optional.empty());

        //when
        boolean result = reviewLikeService.toggleLike(reviewId, likerId);

        //then
        assertThat(result).isTrue();
        verify(reviewLikeRepo, times(1)).save(any(ReviewLike.class));
    }

    @Test
    @DisplayName("이미 누른 좋아요를 다시 누르면, 리뷰 좋아요를 취소한다.")
    void toggleLike_cancel() {
        //given
        Long reviewId = 1L;
        Long likerId = 10L;

        Review mockReview = Review.builder()
            .id(reviewId)
            .likeCount(1L)
            .build();

        ReviewLike mockReviewLike = ReviewLike.builder()
            .review(mockReview)
            .likerId(likerId)
            .build();

        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(mockReview));
        given(reviewLikeRepo.findByReviewAndLikerId(mockReview, likerId)).willReturn(
            Optional.of(mockReviewLike));

        //when
        boolean result = reviewLikeService.toggleLike(reviewId, likerId);

        //then
        assertThat(result).isFalse();
        verify(reviewLikeRepo, times(1)).delete(mockReviewLike);
    }

    @Test
    @DisplayName("베스트 리뷰로 등록한다.")
    void approveBestReview() {
        //given
        Long reviewId = 1L;
        Long sellerId = 10L;
        Long productId= 99L;
        Long storeId = 100L;

        Review mockReview = Review.builder()
            .id(reviewId)
            .isBest(false)
            .productId(productId)
            .bestStatus(ReviewIsBestStatus.CANDIDATE)
            .isExcluded(false)
            .build();

        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(mockReview));

        given(productQueryService.getStoreIdByProductId(productId)).willReturn(storeId);
        given(storeService.isStoreManager(sellerId, storeId)).willReturn(true);

        //when
        reviewLikeService.approveBestReview(reviewId, sellerId);

        //then
        assertThat(mockReview.getIsBest()).isTrue();
        assertThat(mockReview.getBestStatus()).isEqualTo(ReviewIsBestStatus.BEST);
        assertThat(mockReview.isExcluded()).isFalse();
        verify(reviewRepo, times(1)).findById(reviewId);
    }

    @Test
    @DisplayName("베스트 리뷰를 취소한다.")
    void cancelBestReview() {
        //given
        Long reviewId = 1L;
        Long sellerId = 10L;
        Long productId= 99L;
        Long storeId = 100L;

        Review mockReview = Review.builder()
            .id(reviewId)
            .isBest(true)
            .productId(productId)
            .bestStatus(ReviewIsBestStatus.BEST)
            .isExcluded(false)
            .build();

        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(mockReview));

        given(productQueryService.getStoreIdByProductId(productId)).willReturn(storeId);
        given(storeService.isStoreManager(sellerId, storeId)).willReturn(true);

        //when
        reviewLikeService.cancelBestReview(reviewId, sellerId);

        //then
        assertThat(mockReview.getIsBest()).isFalse();
        assertThat(mockReview.getBestStatus()).isEqualTo(ReviewIsBestStatus.CANDIDATE);
        assertThat(mockReview.isExcluded()).isFalse();
        verify(reviewRepo, times(1)).findById(reviewId);
    }

    @Test
    @DisplayName("베스트 리뷰 후보에서 제외한다.")
    void excludeReviewFromBest() {
        //given
        Long reviewId = 1L;
        Long sellerId = 10L;
        Long productId = 99L;
        Long storeId = 100L;

        Review mockReview = Review.builder()
            .id(reviewId)
            .isBest(false)
            .productId(productId)
            .bestStatus(ReviewIsBestStatus.CANDIDATE)
            .isExcluded(false)
            .build();

        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(mockReview));

        given(productQueryService.getStoreIdByProductId(productId)).willReturn(storeId);
        given(storeService.isStoreManager(sellerId, storeId)).willReturn(true);

        //when
        reviewLikeService.excludeReviewFromBest(reviewId, sellerId);

        //then
        assertThat(mockReview.getIsBest()).isFalse();
        assertThat(mockReview.getBestStatus()).isEqualTo(ReviewIsBestStatus.NORMAL);
        assertThat(mockReview.isExcluded()).isTrue();
        verify(reviewRepo, times(1)).findById(reviewId);
    }

}
