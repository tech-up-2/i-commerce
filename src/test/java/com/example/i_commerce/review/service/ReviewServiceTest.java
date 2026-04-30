package com.example.i_commerce.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.exception.ReviewErrorCode;
import com.example.i_commerce.domain.review.repo.ReviewRepository;
import com.example.i_commerce.domain.review.service.ReviewService;
import com.example.i_commerce.domain.review.service.dto.CreateReviewRequest;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepo;

    @InjectMocks
    private ReviewService reviewService;

    @Test
    @DisplayName("성공: 모든 조건이 맞으면 리뷰가 정상적으로 저장된다")
    void createReview_Success() {
        //given
        Long userId = 1L;
        Long orderProductId = 10L;
        Long reviewId = 100L;

        CreateReviewRequest dto = new CreateReviewRequest(
            userId,
            orderProductId,
            "굳굳",
            5,
            List.of("image.jpg")
        );

        //중복된 데이터가 없다고 가정
        given(reviewRepo.existsByOrderProductIdAndUserId(10L, 1L)).willReturn(false);

        Review mockReview = Review.builder().id(reviewId).build();
        given(reviewRepo.save(any(Review.class))).willReturn(mockReview);

        //when
        Long resultId = reviewService.createReview(dto);

        //then
        assertThat(resultId).isEqualTo(100L);
        verify(reviewRepo, times(1)).save(any());
    }

    @Test
    @DisplayName("실패: 해당 주문 상품에 리뷰를 이미 남겼다면 예외 발생")
    void createReview_Fail_AlreadyReviewed() {
        //given
        Long userId = 1L;
        Long orderProductId= 10L;

        CreateReviewRequest dto = new CreateReviewRequest(
            userId,
            orderProductId,
            "리뷰 또 쓰고 싶다",
            5,
            null
        );

        given(reviewRepo.existsByOrderProductIdAndUserId(10L, 1L)).willReturn(true);

        //when&then
        assertThatThrownBy(() -> reviewService.createReview(dto))
            .isInstanceOf(AppException.class)
            .hasFieldOrPropertyWithValue("errorCode", ReviewErrorCode.ALREADY_REVIEWED);

        verify(reviewRepo, never()).save(any(Review.class));

    }

    @Test
    @DisplayName("실패: 별점이 1점 미만 혹은 5점 초과면 예외가 발생한다")
    void createReview_Fail_InvalidStarRating(){
        //given
        CreateReviewRequest overStarRateDto = new CreateReviewRequest(
            1L,
            10L,
            "6점 주고 싶다",
            6,
            null
        );

        //when&then
        assertThatThrownBy(() -> reviewService.createReview(overStarRateDto))
            .isInstanceOf(AppException.class)
            .hasFieldOrPropertyWithValue("errorCode", ReviewErrorCode.INVALID_STAR_RATING);

        verify(reviewRepo, never()).existsByOrderProductIdAndUserId(anyLong(), anyLong());
        verify(reviewRepo, never()).save(any(Review.class));
    }

}
