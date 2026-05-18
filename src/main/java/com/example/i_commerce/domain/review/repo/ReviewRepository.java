package com.example.i_commerce.domain.review.repo;

import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.service.dto.SellerReviewManagementResponse;
import java.util.List;
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
        "WHERE pi.product.id = :productId " +
        "ORDER BY r.createdAt DESC")
    List<Review> findAllByProductId(@Param("productId") Long productId);

    @Query("SELECT r FROM Review r " +
        "JOIN OrderProduct op ON r.orderProductId = op.id " +
        "JOIN ProductItem pi ON op.productSkuId = pi.id " +
        "JOIN pi.product p " +
        "JOIN Store s ON p.storeId = s.id " +
        "WHERE s.sellerId = :sellerId " +
        "ORDER BY r.createdAt DESC")
    List<SellerReviewManagementResponse> findAllBySellerId(@Param("sellerId") Long sellerId);
}

