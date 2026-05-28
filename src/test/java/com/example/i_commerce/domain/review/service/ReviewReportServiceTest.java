package com.example.i_commerce.domain.review.service;


import static com.example.i_commerce.domain.review.entity.enums.ReportType.SPAM;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;
import static org.assertj.core.api.Assertions.assertThat;


import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.ReviewReport;
import com.example.i_commerce.domain.review.entity.enums.ReportProcessStatus;
import com.example.i_commerce.domain.review.entity.enums.ReviewReportStatus;
import com.example.i_commerce.domain.review.repository.ReviewReportRepository;
import com.example.i_commerce.domain.review.repository.ReviewRepository;
import com.example.i_commerce.domain.review.service.dto.CreateReportRequest;
import com.example.i_commerce.global.exception.AppException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.example.i_commerce.domain.review.event.ReviewStatusChangedEvent;
import org.springframework.context.ApplicationEventPublisher;


@ExtendWith(MockitoExtension.class)
public class ReviewReportServiceTest {

    @InjectMocks
    private ReviewReportService reviewReportService;

    @Mock
    private ReviewRepository reviewRepo;

    @Mock
    private ReviewReportRepository reviewReportRepo;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    @DisplayName("타인의 리뷰를 신고하면 신고 데이터가 정상적으로 저장된다.")
    void reportReview_Success() {
        //given
        Long reviewId = 200L;
        Long reporterId = 1L;
        Long writerId = 3L;

        Review review = Review.builder()
            .id(reviewId)
            .userId(writerId)
            .content("타인의 리뷰")
            .version(0L)
            .build();

        CreateReportRequest request = new CreateReportRequest(SPAM, "광고성 댓글입니다.");

        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(review));

        //when
        reviewReportService.createReviewReport(reviewId, reporterId, request);

        //then
        verify(reviewReportRepo, times(1)).save(any(ReviewReport.class));
        assertThat(review.getReportCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("자신의 리뷰를 신고할 경우 예외가 발생한다.")
    void reportReview_Fail_SelfReport() {
        //given
        Long reviewId = 200L;
        Long reporterId = 1L;
        Long writerId = 1L;

        Review review = Review.builder()
            .id(reviewId)
            .userId(writerId)
            .build();

        CreateReportRequest request = new CreateReportRequest(SPAM, "셀프 신고 테스트");

        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(review));

        //when&then
        assertThatThrownBy(
            () -> reviewReportService.createReviewReport(reviewId, reporterId, request))
            .isInstanceOf(AppException.class)
            .hasMessageContaining("자신의 리뷰를 신고할 수 없습니다.");
    }

    @Test
    @DisplayName("신고가 10번 누적되면 리뷰 상태가 HIDDEN_PENDING으로 변경된다")
    void createReport_Hidden() {
        //given
        Long reviewId = 200L;
        Long reporterId = 1L;
        Long writerId = 3L;

        Review review = Review.builder()
            .id(reviewId)
            .userId(writerId)
            .reportCount(9L)
            .reportStatus(ReviewReportStatus.NORMAL)
            .build();

        CreateReportRequest request = new CreateReportRequest(SPAM, "광고입니다");

        given(reviewRepo.findById(reviewId)).willReturn(Optional.of(review));
        given(reviewReportRepo.existsByReporterIdAndReviewId(reporterId, reviewId)).willReturn(
            false);

        //when
        reviewReportService.createReviewReport(reviewId, reporterId, request);

        //then
        assertThat(review.getReportCount()).isEqualTo(10L);
        assertThat(review.getReportStatus()).isEqualTo(ReviewReportStatus.HIDDEN_PENDING);
        verify(reviewReportRepo, times(1)).save(any(ReviewReport.class));
    }

    @Test
    @DisplayName("관리자가 신고를 거절하면 리뷰가 NORMAL로 복구되고 신고수가 초기화된다.")
    void rejectReport_Success() {
        //given
        Long reportId = 10L;
        Long adminId = 99L;

        Review review = Review.builder()
            .id(200L)
            .reportCount(10L)
            .reportStatus(ReviewReportStatus.HIDDEN_PENDING)
            .build();

        ReviewReport report = ReviewReport.builder()
            .id(reportId)
            .review(review)
            .processStatus(ReportProcessStatus.PENDING)
            .build();

        given(reviewReportRepo.findById(reportId)).willReturn(Optional.of(report));

        //when
        reviewReportService.rejectReport(reportId, adminId);

        //then
        assertThat(report.getProcessStatus()).isEqualTo(ReportProcessStatus.REJECTED);
        assertThat(review.getReportStatus()).isEqualTo(ReviewReportStatus.NORMAL);
        assertThat(review.getReportCount()).isEqualTo(0L);
    }

    @Test
    @DisplayName("관리자가 신고를 승인하면 리뷰 상태가 HIDDEN이 되면서 이벤트가 발생한다.")
    void approveReport_Success() {
        //given
        Long reportId = 1L;
        Long adminId = 10L;

        Review review = Review.builder()
            .id(200L)
            .userId(2L)
            .reportStatus(ReviewReportStatus.HIDDEN_PENDING)
            .build();

        ReviewReport report = ReviewReport.builder()
            .id(reportId)
            .review(review)
            .status(ReviewReportStatus.HIDDEN_PENDING)
            .build();

        given(reviewReportRepo.findById(reportId)).willReturn(Optional.of(report));

        //when
        reviewReportService.approveReport(reportId, adminId);

        //then
        assertThat(report.getProcessStatus()).isEqualTo(ReportProcessStatus.APPROVED);
        assertThat(review.getReportStatus()).isEqualTo(ReviewReportStatus.HIDDEN);

        verify(eventPublisher, times(1)).publishEvent(any(ReviewStatusChangedEvent.class));
    }

    @Test
    @DisplayName("성공: 관리자 승인 시 정확한 신고자 ID와 메시지가 포함된 이벤트가 발행된다.")
    void approveReport_EventContent_Check() {
        //given
        Long reporterId = 1L;
        Long reportId = 100L;
        Long adminId = 10L;

        Review review = Review.builder()
            .id(200L)
            .userId(2L)
            .reportStatus(ReviewReportStatus.HIDDEN_PENDING)
            .build();

        ReviewReport report = ReviewReport.builder()
            .id(reportId)
            .reporterId(reporterId)
            .review(review)
            .processStatus(ReportProcessStatus.PENDING)
            .build();

        given(reviewReportRepo.findById(reportId)).willReturn(Optional.of(report));

        ArgumentCaptor<ReviewStatusChangedEvent> eventCaptor = ArgumentCaptor.forClass(ReviewStatusChangedEvent.class);

        //when
        reviewReportService.approveReport(reportId, adminId);

        //then
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        ReviewStatusChangedEvent capturedEvent = eventCaptor.getValue();

        assertThat(capturedEvent.getReporterId()).isEqualTo(reporterId);
        assertThat(capturedEvent.getMessage()).contains("처리되었습니다");
    }
}
