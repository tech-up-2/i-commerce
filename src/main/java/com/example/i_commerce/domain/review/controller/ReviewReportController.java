package com.example.i_commerce.domain.review.controller;

import com.example.i_commerce.domain.review.service.ReviewReportService;
import com.example.i_commerce.domain.review.service.dto.CreateReportRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Review API", description = "리뷰 관련 API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewReportController {

    @Operation(summary = "리뷰 신고", description = "사용자가 특정 리뷰를 신고할 때 사용한다.")
    @PostMapping("/reviews/{reviewId}/reports")
    @PreAuthorize("@authChecker.canReportReview()")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> createReport(
        @PathVariable Long reviewId,
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody CreateReportRequest dto
    ) {
        reviewReportService.createReviewReport(reviewId, principal.getId(), dto);
        return ApiResponse.success();
    }

    private final ReviewReportService reviewReportService;

    @Operation(summary = "신고 승인", description = "관리자는 특정 리뷰 신고를 승인할 수 있다.")
    @PreAuthorize("@authChecker.canManageReviewAsAdmin()")
    @PatchMapping("/admin/reports/{reportId}/approve")
    public ApiResponse<Void> approveReport(
        @PathVariable Long reportId,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        reviewReportService.approveReport(reportId, principal.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "신고 반려", description = "관리자는 특정 리뷰 신고에 대한 반려를 할 수 있다.")
    @PreAuthorize("@authChecker.canManageReviewAsAdmin()")
    @PatchMapping("/admin/reports/{reportId}/reject")
    public ApiResponse<Void> rejectReport(
        @PathVariable Long reportId,
        @AuthenticationPrincipal CustomUserPrincipal principal) {
        reviewReportService.rejectReport(reportId, principal.getId());
        return ApiResponse.success();
    }
}
