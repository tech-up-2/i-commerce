package com.example.i_commerce.domain.review.repository;


import static org.assertj.core.api.Assertions.assertThat;

import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.ReviewLike;
import com.example.i_commerce.domain.review.entity.enums.ReviewIsBestStatus;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ReviewLikeRepositoryTest.TestCacheConfig.class)
class ReviewLikeRepositoryTest {

    @Autowired
    private ReviewLikeRepository reviewLikeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("리뷰와 사용자 ID로 좋아요 기록을 정확히 조회한다")
    void findByReviewAndLikerId_Success() {
        // given
        Review review = Review.builder()
            .orderProductId(1L)
            .userId(1L)
            .content("정말 좋아유")
            .starRate(5)
            .likeCount(2L)
            .bestStatus(ReviewIsBestStatus.NORMAL)
            .isExcluded(false)
            .build();
        entityManager.persist(review);

        ReviewLike like = ReviewLike.builder()
            .review(review)
            .likerId(28L)
            .build();
        entityManager.persist(like);
        entityManager.flush();

        // when
        Optional<ReviewLike> result = reviewLikeRepository.findByReviewAndLikerId(review, 28L);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getLikerId()).isEqualTo(28L);
    }

    @TestConfiguration
    static class TestCacheConfig {
        @Bean
        public CacheManager cacheManager() {
            return new ConcurrentMapCacheManager("forbiddenWords");
        }
    }
}