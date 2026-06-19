package com.example.i_commerce.domain.review.repository;

import com.example.i_commerce.domain.review.entity.ReviewComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewCommentRepository extends JpaRepository<ReviewComment, Long> {

    List<ReviewComment> findByReviewId(Long reviewId);

}
