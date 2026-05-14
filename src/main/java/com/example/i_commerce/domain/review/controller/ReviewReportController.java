package com.example.i_commerce.domain.review.controller;

import com.example.i_commerce.domain.review.service.ReviewReportService;
import com.example.i_commerce.domain.review.service.dto.CreateReportRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@SecurityRequirement(name = "BearerAuth")
public class ReviewReportController {

    private final ReviewReportService reviewReportService;

    @PostMapping("/reviews/{reviewId}/reports")
    @Operation(summary = "리뷰 신고", description = "사용자가 특정 리뷰를 신고할 때 사용합니다.")
    @PreAuthorize("@authChecker.canReportReview()")
    public ResponseEntity<ApiResponse<Void>> createReport(
        @PathVariable Long reviewId,
        @RequestParam Long reporterId,
        @RequestBody CreateReportRequest dto
    ) {
        reviewReportService.createReviewReport(reviewId, reporterId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    @PatchMapping("/admin/reports/{reportId}/approve")
    @Operation(summary = "신고 승인", description = "관리자는 특정 리뷰 신고를 승인할 수 있다.")
    @PreAuthorize("@authChecker.canManageReviewAsAdmin()")
    public ResponseEntity<ApiResponse<Void>> approveReport(
        @PathVariable Long reportId,
        @RequestParam Long adminId
    ) {
        reviewReportService.approveReport(reportId, adminId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PatchMapping("/admin/reports/{reportId}/reject")
    @Operation(summary = "신고 반려", description = "관리자는 특정 리뷰 신고에 대한 반려를 할 수 있다.")
    @PreAuthorize("@authChecker.canManageReviewAsAdmin()")
    public ResponseEntity<ApiResponse<Void>> rejectReport(
        @PathVariable Long reportId,
        @RequestParam Long adminId) {
        reviewReportService.rejectReport(reportId, adminId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
