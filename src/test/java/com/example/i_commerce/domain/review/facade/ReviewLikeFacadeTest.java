package com.example.i_commerce.domain.review.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.facade.ReviewLikeFacade;
import com.example.i_commerce.domain.review.service.ReviewLikeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

@ExtendWith(MockitoExtension.class)
public class ReviewLikeFacadeTest {

    @Mock
    private ReviewLikeService reviewLikeService;

    @InjectMocks
    private ReviewLikeFacade reviewLikeFacade;

    @Test
    @DisplayName("낙관적 락 충돌이 발생하면 성공할 때까지 재시도한다")
    void toggleLike_RetryOnConflict() throws InterruptedException {
        //given
        Long reviewId = 1L;
        Long likerId = 100L;

        given(reviewLikeService.toggleLike(reviewId, likerId))
            .willThrow(new ObjectOptimisticLockingFailureException(Review.class, "conflict"))
            .willThrow(new ObjectOptimisticLockingFailureException(Review.class, "conflict"))
            .willReturn(true);

        //when
        boolean result = reviewLikeFacade.toggleLikeWithRetry(reviewId, likerId);

        //then
        assertThat(result).isTrue();
        verify(reviewLikeService, times(3)).toggleLike(reviewId, likerId);
    }

}
