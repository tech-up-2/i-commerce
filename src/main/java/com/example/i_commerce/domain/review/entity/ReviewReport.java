package com.example.i_commerce.domain.review.entity;

import com.example.i_commerce.domain.review.entity.enums.ReviewReportStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "review_reports")
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReviewReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

//    @Column(nullable = false)
//    private Long reviewId;

    @Column(nullable = false, length = 50)
    private Long reporterId;

    @Column(nullable = false)
    private Long adminId;

    @Column(length = 20)
    private String reportType;

    @Column(columnDefinition = "TEXT")
    private String reportReason;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReviewReportStatus status;
}
