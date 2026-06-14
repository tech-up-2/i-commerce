package com.example.i_commerce.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.ReviewReport;
import com.example.i_commerce.domain.review.entity.enums.ReportProcessStatus;
import com.example.i_commerce.domain.review.entity.enums.ReportType;
import com.example.i_commerce.domain.review.entity.enums.ReviewReportStatus;
import com.example.i_commerce.domain.review.event.ReviewStatusChangedEvent;
import com.example.i_commerce.domain.review.repository.ReviewReportRepository;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
import com.example.i_commerce.domain.review.service.dto.CreateReportRequest;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class ReviewReportServiceUnitTest {

    @InjectMocks
    private ReviewReportService reviewReportService;

    @Mock
    private ReviewReportRepository reviewReportRepo;
    
    @Mock
    private ReviewRepository reviewRepo;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("리뷰 신고를 할 수 있다.")
    void createReviewReport() {
        //given
        Long reviewId = 1L;
        Long userId = 5L;
        Long reporterId = 10L;
        Long expectedReportId = 99L;

        Review mockReview = Review.builder()
            .id(reviewId)
            .userId(userId)
            .reportCount(0L)
            .build();

        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(mockReview));

        given(reviewReportRepo.existsByReporterIdAndReviewId(reporterId, reviewId))
            .willReturn(false);

        given(reviewReportRepo.save(any(ReviewReport.class))).willAnswer(invocation -> {
            ReviewReport report = invocation.getArgument(0);
            ReflectionTestUtils.setField(report, "id", expectedReportId);
            return report;
        });
        
        CreateReportRequest request = new CreateReportRequest("광고성 댓글입니다.", ReportType.SPAM);

        //when
        Long actualReportId = reviewReportService.createReviewReport(reviewId, reporterId, request);

        //then
        verify(reviewReportRepo, times(1)).save(any(ReviewReport.class));
        assertThat(mockReview.getReportCount()).isEqualTo(1L);

        assertThat(actualReportId).isEqualTo(expectedReportId);
    }

    @Test
    @DisplayName("신고 내역을 승인한다.")
    void approveReport() {
        //given
        Long reviewId = 1L;
        Long reportId = 10L;
        Long reporterId = 11L;
        Long adminId= 100L;

        Review mockReview = Review.builder()
            .id(reviewId)
            .reportStatus(ReviewReportStatus.NORMAL)
            .build();

        ReviewReport report = ReviewReport.builder()
            .id(reportId)
            .review(mockReview)
            .adminId(adminId)
            .reporterId(reporterId)
            .processStatus(ReportProcessStatus.PENDING)
            .build();

        given(reviewReportRepo.findById(reportId)).willReturn(Optional.of(report));

        //when
        reviewReportService.approveReport(reportId, adminId);

        //then
        assertThat(report.getProcessStatus()).isEqualTo(ReportProcessStatus.APPROVED);
        assertThat(mockReview.getReportStatus()).isEqualTo(ReviewReportStatus.HIDDEN);
        verify(reviewReportRepo, times(1)).findById(reportId);
        verify(eventPublisher, times(1)).publishEvent(any(ReviewStatusChangedEvent.class));
    }

    @Test
    @DisplayName("신고 내역을 거절한다.")
    void rejectReport() {
        //given
        Long reportId = 1L;
        Long reporterId = 9L;
        Long adminId = 10L;
        Long reviewId = 100L;

        Review mockReview = Review.builder()
            .id(reviewId)
            .reportStatus(ReviewReportStatus.HIDDEN_PENDING)
            .reportCount(11L)
            .build();

        ReviewReport report = ReviewReport.builder()
            .review(mockReview)
            .id(reportId)
            .adminId(adminId)
            .reporterId(reporterId)
            .processStatus(ReportProcessStatus.PENDING)
            .build();

        given(reviewReportRepo.findById(reportId)).willReturn(Optional.of(report));

        //when
        reviewReportService.rejectReport(reportId, adminId);

        //then
        assertThat(mockReview.getReportStatus()).isEqualTo(ReviewReportStatus.NORMAL);
        assertThat(mockReview.getReportCount()).isEqualTo(0L);
        assertThat(report.getProcessStatus()).isEqualTo(ReportProcessStatus.REJECTED);
        verify(reviewReportRepo, times(1)).findById(reportId);
    }

}
