package com.example.i_commerce.domain.review.repository;

import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.service.dto.SellerReviewManagementResponse;
import com.example.i_commerce.global.common.response.SliceResponse;
import java.util.List;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByOrderProductIdAndUserId(Long orderProductId, Long userId);

    List<Review> findAllByOrderProductIdAndDeletedAtIsNull(Long orderProductId);

    @Query("SELECT COUNT(r) > 0 FROM Review r " +
        "JOIN OrderProduct op ON r.orderProductId = op.id " +
        "JOIN ProductItem pi ON op.productSkuId = pi.id " +
        "JOIN pi.product p " +
        "JOIN Store s ON p.storeId = s.id " +
        "WHERE r.id = :reviewId AND s.sellerId = :sellerId")
    boolean existsByIdAndSellerId(@Param("reviewId") Long reviewId, @Param("sellerId") Long sellerId);

    @Query("SELECT r FROM Review r " +
        "JOIN OrderProduct op ON r.orderProductId = op.id " +
        "JOIN ProductItem pi ON op.productSkuId = pi.id " +
        "WHERE pi.product.id = :productId")
    Slice<Review> findSliceByProductId(@Param("productId") Long productId, Pageable pageable);

    @Query("SELECT r FROM Review r " +
        "JOIN OrderProduct op ON r.orderProductId = op.id " +
        "JOIN ProductItem pi ON op.productSkuId = pi.id " +
        "JOIN pi.product p " +
        "JOIN Store s ON p.storeId = s.id " +
        "WHERE s.sellerId = :sellerId " +
        "ORDER BY r.createdAt DESC")
    List<SellerReviewManagementResponse> findAllBySellerId(@Param("sellerId") Long sellerId);

    @Query("SELECT COUNT(op) > 0 FROM OrderProduct op " +
        "JOIN op.order o " +
        "WHERE op.id = :orderProductId " +
        "AND o.userId = :userId " +
        "AND o.orderStatus = :status")
    boolean isReviewableStatus(
        @Param("orderProductId") Long orderProductId,
        @Param("userId") Long userId,
        @Param("status") OrderStatus status
    );

    @Query("SELECT r FROM Review r " +
        "JOIN OrderProduct op ON r.orderProductId = op.id " +
        "JOIN ProductItem pi ON op.productSkuId = pi.id " +
        "WHERE (:displayOptionName IS NULL OR pi.displayOptionName = :displayOptionName) " +
        "AND (:keyword IS NULL OR r.content LIKE CONCAT('%',:keyword, '%')) " +
        "AND (:starRate IS NULL OR r.starRate = :starRate)")
    Slice<Review> searchReviews(
        @Param("displayOptionName") String displayOptionName,
        @Param("keyword") String keyword,
        @Param("starRate") Integer starRate,
        Pageable pageable
    );

    @Query("SELECT r.starRate AS starRate, COUNT(r) AS count " +
        "FROM Review r " +
        "WHERE r.productId = :productId " +
        "GROUP BY r.starRate")
    List<StarRateCountProjection> getStarRateStats(@Param("productId") Long productId);

}

