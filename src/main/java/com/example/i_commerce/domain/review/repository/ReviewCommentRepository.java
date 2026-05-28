package com.example.i_commerce.domain.review.repository;

import com.example.i_commerce.domain.review.entity.ReviewComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {

    boolean existsByReviewId(Long reviewId);


}
