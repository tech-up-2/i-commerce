package com.example.i_commerce.domain.review.entity;

import com.example.i_commerce.domain.review.entity.enums.ReportProcessStatus;
import com.example.i_commerce.domain.review.entity.enums.ReportType;
import com.example.i_commerce.domain.review.entity.enums.ReviewReportStatus;
import com.example.i_commerce.domain.review.exception.ReviewErrorCode;
import com.example.i_commerce.global.exception.AppException;
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

    @Column(nullable = false, length = 50)
    private Long reporterId;

    private Long adminId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ReportType reportType;

    @Column(columnDefinition = "TEXT")
    private String reportReason;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    @Builder.Default
    private ReviewReportStatus status = ReviewReportStatus.NORMAL;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ReportProcessStatus processStatus = ReportProcessStatus.PENDING;

    public void approve(Long adminId) {
        validateAdmin(adminId);
        validatePendingStatus();

        this.adminId = adminId;
        this.processStatus = ReportProcessStatus.APPROVED;
    }

    public void reject(Long adminId) {
        validateAdmin(adminId);
        validatePendingStatus();

        this.adminId = adminId;
        this.processStatus = ReportProcessStatus.REJECTED;
    }

    private void validateAdmin(Long adminId) {
        if (adminId == null) {
            throw new AppException(ReviewErrorCode.ADMIN_ID_REQUIRED);
        }
        if (this.adminId != null && !this.adminId.equals(adminId)) {
            throw new AppException(ReviewErrorCode.ALREADY_ASSIGNED_ADMIN);
        }
    }

    private void validatePendingStatus() {
        if (this.processStatus != ReportProcessStatus.PENDING) {
            throw new AppException(ReviewErrorCode.ALREADY_PROCESSED);
        }
    }

}
