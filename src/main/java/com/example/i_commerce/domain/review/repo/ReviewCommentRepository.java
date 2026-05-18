package com.example.i_commerce.domain.review.repo;

import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.ReviewComment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {

    boolean existsByReviewId(Long reviewId);


}
