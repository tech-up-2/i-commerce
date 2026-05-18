package com.example.i_commerce.domain.review.repo;

import com.example.i_commerce.domain.review.entity.ReviewReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewReportRepository extends JpaRepository<ReviewReport,Long> {

    boolean existsByReporterIdAndReviewId(Long reporterId, Long reviewId);
}
