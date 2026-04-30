package com.example.i_commerce.domain.review.repo;

import com.example.i_commerce.domain.review.entity.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByOrderProductIdAndUserId(Long orderProductId, Long userId);

    List<Review> findAllByOrderProductIdAndDeletedAtIsNull(Long orderProductId);

}
