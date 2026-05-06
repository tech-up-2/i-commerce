package com.example.i_commerce.domain.review.repo;

import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.ReviewLike;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select rl from ReviewLike rl where rl.review = :review and rl.likerId = :likerId")
    Optional<ReviewLike> findByReviewAndLikerId(
        @Param("review") Review review,
        @Param("likerId") Long likerId);

}
