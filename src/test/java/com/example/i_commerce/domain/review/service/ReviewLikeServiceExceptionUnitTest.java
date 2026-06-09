package com.example.i_commerce.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.example.i_commerce.domain.member.service.store.StoreService;
import com.example.i_commerce.domain.product.application.service.ProductQueryService;
import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
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
public class ReviewLikeServiceExceptionUnitTest {

    @InjectMocks
    private ReviewLikeService reviewLikeService;

    @Mock
    private ReviewRepository reviewRepo;

    @Mock
    private ProductQueryService productQueryService;

    @Mock
    private StoreService storeService;

    @Test
    @DisplayName("실패: 자신의 상점이 아닌 다른 상점의 리뷰를 베스트로 선정하려 하면 INVALID_PERMISSION 예외가 발생한다.")
    void approveBestReview_notStoreManager() {
        // given
        Long reviewId = 1L;
        Long hackerSellerId = 99L;
        Long productId = 100L;
        Long storeId = 500L;

        Review mockReview = Review.builder()
            .id(reviewId)
            .productId(productId)
            .build();

        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(mockReview));

        given(productQueryService.getStoreIdByProductId(productId)).willReturn(storeId);

        given(storeService.isStoreManager(hackerSellerId, storeId)).willReturn(false);

        // when
        AppException exception = assertThrows(AppException.class,
            () -> reviewLikeService.approveBestReview(reviewId, hackerSellerId)
        );

        // then
        assertThat(exception.getErrorCode()).isEqualTo(CommonErrorCode.INVALID_PERMISSION);
    }
}
