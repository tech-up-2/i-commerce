package com.example.i_commerce.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.enums.ReviewIsBestStatus;
import com.example.i_commerce.domain.review.repository.ReviewLikeRepository;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ReviewLikeServiceTest {

    @Mock
    private ReviewRepository reviewRepo;

    @Mock
    private ReviewLikeRepository reviewLikeRepo;

    @InjectMocks
    private ReviewLikeService reviewLikeService;

    private Review review;
    private final Long reviewId = 1L;
    private final Long likerId = 100L;
    private final double THRESHOLD = 80.0;

    @BeforeEach
    void setUp() {
        review = Review.builder()
            .id(reviewId)
            .content("정말 좋아유")
            .starRate(5)
            .likeCount(0L)
            .bestStatus(ReviewIsBestStatus.NORMAL)
            .isExcluded(false)
            .build();
    }

    @Test
    @DisplayName("판매자는 점수와 상관없이 어떤 리뷰든 BEST로 지정할 수 있다.")
    void approveBestReview_ForceSuccess() {
        //given
        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(review));

        //when
        reviewLikeService.approveBestReview(reviewId);

        //then
        assertThat(review.getBestStatus()).isEqualTo(ReviewIsBestStatus.BEST);
        assertThat(review.getIsBest()).isTrue();
    }

    @Test
    @DisplayName("후보에서 제외 처리된 리뷰는 좋아요가 눌려도 CANDIDATE로 변경되지 않는다.")
    void toggleLike_OnExcludeReview_RemainNormal() {
        //given
        review.excludeFromBest();
        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(review));
        given(reviewLikeRepo.findByReviewAndLikerId(review, likerId)).willReturn(Optional.empty());

        //when
        reviewLikeService.toggleLike(reviewId, likerId);

        //then
        assertThat(review.getLikeCount()).isEqualTo(1L);
        assertThat(review.isExcluded()).isTrue();
        assertThat(review.getBestStatus()).isEqualTo(ReviewIsBestStatus.NORMAL);
    }

    @Test
    @DisplayName("베스트 리뷰를 취소하면 다시 점수에 따라 상태가 재결정된다")
    void cancelBestReview_AndReEvaluate() {
        //given
        review.approveAsBest();
        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(review));

        //when
        reviewLikeService.cancelBestReview(reviewId);

        //then
        assertThat(review.getBestStatus()).isEqualTo(ReviewIsBestStatus.CANDIDATE);
        assertThat(review.getIsBest()).isFalse();
    }

    @Test
    @DisplayName("특정 리뷰를 베스트 후보에서 제외하면 즉시 NORMAL 상태로 강등된다")
    void excludeReview_ImmediatelyDowngrade() {
        // given
        review.checkBestEligibility(80.0); // 만약 점수가 높아 후보(CANDIDATE)인 상태였다면
        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(review));

        // when
        reviewLikeService.excludeReviewFromBest(reviewId);

        // then
        assertThat(review.isExcluded()).isTrue();
        assertThat(review.getBestStatus()).isEqualTo(ReviewIsBestStatus.NORMAL);
    }
}
