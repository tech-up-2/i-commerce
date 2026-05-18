package com.example.i_commerce.domain.review.controller;

import com.example.i_commerce.domain.review.service.ReviewReportService;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin Review API", description = "관리자 리뷰 관리 API")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("@authChecker.canManageReviewAsAdmin()")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/reviews")
public class AdminReviewController {

    private final ReviewReportService reviewReportService;

    @Operation(summary = "신고 승인", description = "관리자는 특정 리뷰 신고를 승인할 수 있다.")
    @PatchMapping("/reports/{reportId}/approve")
    public ApiResponse<Void> approveReport(
        @PathVariable Long reportId,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        reviewReportService.approveReport(reportId, principal.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "신고 반려", description = "관리자는 특정 리뷰 신고에 대한 반려를 할 수 있다.")
    @PatchMapping("/reports/{reportId}/reject")
    public ApiResponse<Void> rejectReport(
        @PathVariable Long reportId,
        @AuthenticationPrincipal CustomUserPrincipal principal) {
        reviewReportService.rejectReport(reportId, principal.getId());
        return ApiResponse.success();
    }
}
