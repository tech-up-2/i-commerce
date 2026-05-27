package com.example.i_commerce.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.exception.ReviewErrorCode;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
import com.example.i_commerce.domain.review.service.dto.CreateReviewRequest;
import com.example.i_commerce.domain.review.validator.ReviewForbiddenWordValidator;
import com.example.i_commerce.global.exception.AppException;
import com.example.i_commerce.global.s3.service.S3ImageService;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepo;

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewForbiddenWordValidator reviewForbiddenWordValidator;

    @Mock
    private S3ImageService s3ImageService;

    @Test
    @DisplayName("성공: 모든 조건이 맞으면 리뷰가 정상적으로 저장된다")
    void createReview_Success() {
        //given
        Long userId = 1L;
        Long orderProductId = 10L;
        Long reviewId = 100L;

        CreateReviewRequest request = new CreateReviewRequest("굳굳", 5);
        List<MultipartFile> imageFiles = List.of();

        given(reviewRepo.isReviewableStatus(orderProductId, userId, OrderStatus.COMPLETED))
            .willReturn(true);

        given(reviewRepo.existsByOrderProductIdAndUserId(10L, 1L)).willReturn(false);

        Review mockReview = Review.builder().id(reviewId).build();
        given(reviewRepo.save(any(Review.class))).willReturn(mockReview);

        //when
        Long resultId = reviewService.createReview(orderProductId, userId, request, imageFiles);

        //then
        assertThat(resultId).isEqualTo(100L);
        verify(reviewRepo, times(1)).save(any());
    }

    @Test
    @DisplayName("실패: 주문 상태가 구매 확정(COMPLETED)이 아니면 리뷰 작성이 불가능하다")
    void createReview_Fail_NotCompletedStatus() {
        // given
        Long userId = 1L;
        Long orderProductId = 10L;
        CreateReviewRequest request = new CreateReviewRequest("내 돈 내 산 리뷰", 5);
        List<MultipartFile> imageFiles = List.of();

        given(reviewRepo.isReviewableStatus(orderProductId, userId, OrderStatus.COMPLETED))
            .willReturn(false);

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(orderProductId, userId, request, imageFiles))
            .isInstanceOf(AppException.class)
            .hasFieldOrPropertyWithValue("errorCode", ReviewErrorCode.NOT_ACTUAL_BUYER);

        verify(reviewRepo, never()).save(any(Review.class));
        verify(reviewRepo, never()).existsByOrderProductIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("실패: 해당 주문 상품에 리뷰를 이미 남겼다면 예외 발생")
    void createReview_Fail_AlreadyReviewed() {
        //given
        Long userId = 1L;
        Long orderProductId= 10L;

        CreateReviewRequest request = new CreateReviewRequest("리뷰 또 쓰고 싶다", 5);
        List<MultipartFile> imageFiles = List.of();

        given(reviewRepo.isReviewableStatus(orderProductId, userId, OrderStatus.COMPLETED))
            .willReturn(true);

        given(reviewRepo.existsByOrderProductIdAndUserId(10L, 1L)).willReturn(true);

        //when&then
        assertThatThrownBy(() -> reviewService.createReview(orderProductId, userId, request, imageFiles))
            .isInstanceOf(AppException.class)
            .hasFieldOrPropertyWithValue("errorCode", ReviewErrorCode.ALREADY_REVIEWED);

        verify(reviewRepo, never()).save(any(Review.class));

    }

    @Test
    @DisplayName("실패: 별점이 1점 미만 혹은 5점 초과면 예외가 발생한다")
    void createReview_Fail_InvalidStarRating(){
        //given

        Long userId = 1L;
        Long orderProductId = 10L;

        CreateReviewRequest request = new CreateReviewRequest("6점 주고 싶다", 6);
        List<MultipartFile> imageFiles = List.of();

        //when&then
        assertThatThrownBy(() -> reviewService.createReview(orderProductId, userId, request, imageFiles))
            .isInstanceOf(AppException.class)
            .hasFieldOrPropertyWithValue("errorCode", ReviewErrorCode.INVALID_STAR_RATING);

        verify(reviewRepo, never()).existsByOrderProductIdAndUserId(anyLong(), anyLong());
        verify(reviewRepo, never()).save(any(Review.class));
    }

}
