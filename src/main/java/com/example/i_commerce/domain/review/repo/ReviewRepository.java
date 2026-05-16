package com.example.i_commerce.domain.review.repo;

import com.example.i_commerce.domain.review.entity.Review;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByOrderProductIdAndUserId(Long orderProductId, Long userId);

    List<Review> findAllByOrderProductIdAndDeletedAtIsNull(Long orderProductId);

    @Query("SELECT r FROM Review r " +
        "JOIN OrderProduct op ON r.orderProductId = op.id " +
        "JOIN ProductItem pi ON op.productSkuId = pi.id " +
        "LEFT JOIN FETCH r.comment " +
        "WHERE pi.product.id = :productId " +
        "AND r.deletedAt IS NULL")
    List<Review> findAllByProductId(@Param("productId") Long productId);
}

