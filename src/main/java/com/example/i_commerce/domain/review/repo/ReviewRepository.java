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
        "JOIN OrderProduct op ON r.orderProductId = op.id " +    // 1단계: 주문상품 연결
        "JOIN ProductItem pi ON op.productSkuId = pi.id " +     // 2단계: ProductItem으로 연결!
        "LEFT JOIN FETCH r.comment " +                          // 답글 미리 가져오기
        "WHERE pi.product.id = :productId " +                   // 💡 필드명이 아니라 객체 그래프(pi.product.id)로 접근!
        "AND r.deletedAt IS NULL")
    List<Review> findAllByProductId(@Param("productId") Long productId);
}

