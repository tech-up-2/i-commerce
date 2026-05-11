package com.example.i_commerce.domain.review.service;

import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.ReviewReport;
import com.example.i_commerce.domain.review.entity.enums.ReportProcessStatus;
import com.example.i_commerce.domain.review.event.ReportApprovedEvent;
import com.example.i_commerce.domain.review.exception.ReviewErrorCode;
import com.example.i_commerce.domain.review.repo.ReviewReportRepository;
import com.example.i_commerce.domain.review.repo.ReviewRepository;
import com.example.i_commerce.domain.review.service.dto.CreateReportRequest;
import com.example.i_commerce.global.exception.AppException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewReportService {

    private final ReviewRepository reviewRepo;
    private final ReviewReportRepository reviewReportRepo;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void createReviewReport(Long reviewId, Long reporterId, CreateReportRequest dto) {

        Review review = reviewRepo.findById(reviewId)
            .orElseThrow(() -> new AppException(ReviewErrorCode.REVIEW_NOT_FOUND));

        if(reviewReportRepo.existsByReporterIdAndReviewId(reporterId, reviewId)){
            throw new AppException(ReviewErrorCode.ALREADY_REPORT);
        }

        if (review.getUserId().equals(reporterId)) {
            throw new AppException(ReviewErrorCode.INVALID_SELF_REPORTING);
        }

        ReviewReport report = ReviewReport.builder()
            .reporterId(reporterId)
            .review(review)
            .reportType(dto.getReportType())
            .reportReason(dto.getReason())
            .build();

        reviewReportRepo.save(report);

        review.incrementReportCount();

    }

    @Transactional
    public void approveReport(Long reportId, Long adminId) {
        ReviewReport report = reviewReportRepo.findById(reportId)
            .orElseThrow(() -> new AppException(ReviewErrorCode.REPORT_NOT_FOUND));

        report.assignAdmin(adminId);

        report.complete();

        eventPublisher.publishEvent(new ReportApprovedEvent(
            report.getReporterId(),
            "신고하신 리뷰가 처리되었습니다."
        ));
    }

    @Transactional
    public void rejectReport(Long reportId, Long adminId) {
        ReviewReport report = reviewReportRepo.findById(reportId)
            .orElseThrow(() -> new AppException(ReviewErrorCode.REPORT_NOT_FOUND));

        report.assignAdmin(adminId);
        report.reject();
    }
}
