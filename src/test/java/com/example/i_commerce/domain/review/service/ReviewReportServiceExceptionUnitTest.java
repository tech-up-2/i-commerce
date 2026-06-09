package com.example.i_commerce.domain.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.ReviewReport;
import com.example.i_commerce.domain.review.entity.enums.ReportType;
import com.example.i_commerce.domain.review.exception.ReviewErrorCode;
import com.example.i_commerce.domain.review.repository.ReviewReportRepository;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
import com.example.i_commerce.domain.review.service.dto.CreateReportRequest;
import com.example.i_commerce.global.exception.AppException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ReviewReportServiceExceptionUnitTest {

    @InjectMocks
    private ReviewReportService reviewReportService;

    @Mock
    private ReviewRepository reviewRepo;

    @Mock
    private ReviewReportRepository reviewReportRepo;

    @Test
    @DisplayName("이미 신고한 리뷰에 신고를 시도한다.")
    void alreadyReport() {
        //given
        Long reviewId = 1L;
        Long reporterId = 10L;

        Review mockReview = Review.builder()
            .id(reviewId)
            .build();

        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(mockReview));

        given(reviewReportRepo.existsByReporterIdAndReviewId(reporterId, reviewId)).willReturn(true);

        CreateReportRequest request = new CreateReportRequest("광고성 댓글", ReportType.SPAM);

        //when
        AppException appException = assertThrows(AppException.class,
            () -> reviewReportService.createReviewReport(reviewId, reporterId, request));

        //then
        assertThat(appException.getErrorCode()).isEqualTo(ReviewErrorCode.ALREADY_REPORT);
    }

    @Test
    @DisplayName("자신의 리뷰는 신고할 수 없다.")
    void invalidSelfReport() {
        //given
        Long reviewId = 1L;
        Long reporterId = 10L;

        Review mockReview = Review.builder()
            .id(reviewId)
            .userId(reporterId)
            .build();

        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(mockReview));

        given(reviewReportRepo.existsByReporterIdAndReviewId(reporterId,reviewId)).willReturn(false);

        CreateReportRequest request = new CreateReportRequest("광고성 댓글", ReportType.SPAM);

        //when
        AppException appException = assertThrows(AppException.class,
            () -> reviewReportService.createReviewReport(reviewId, reporterId, request));


        //then
        assertThat(appException.getErrorCode()).isEqualTo(ReviewErrorCode.INVALID_SELF_REPORTING);
    }
}
