package com.example.i_commerce.domain.review.repository;

import com.example.i_commerce.domain.order.entity.emuns.OrderStatus;
import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.enums.ReviewStatus;
import com.example.i_commerce.domain.review.service.dto.SellerReviewManagementResponse;
import com.example.i_commerce.global.common.response.SliceResponse;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserIdAndOrderProductIdAndStatus(Long userId, Long orderProductId, ReviewStatus status);

    Optional<Review> findByIdAndStatus(Long id, ReviewStatus status);

    Slice<Review> findByProductIdAndStatus(Long productId, ReviewStatus status, Pageable pageable);

    List<Review> findAllByProductIdAndStatus(Long productId, ReviewStatus status);

    @Query("SELECT r FROM Review r " +
        "WHERE r.productId = :productId " +
        "AND (:displayOptionName IS NULL OR r.displayOptionName = :displayOptionName) " +
        "AND (:keyword IS NULL OR r.content LIKE CONCAT('%', CAST(:keyword AS string), '%')) " +
        "AND (:starRate IS NULL OR r.starRate = :starRate)")
    Slice<Review> searchReviews(
        @Param("productId") Long productId,
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

    Slice<Review> findAllByProductIdIn(List<Long> productIds, Pageable pageable);

    //리뷰 좋아요 - 비관락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Review r where r.id = :id")
    Optional<Review> findByIdForUpdate(@Param("id") Long id);
}

