package com.example.i_commerce.domain.review.controller;

import com.example.i_commerce.domain.review.service.ReviewReportService;
import com.example.i_commerce.domain.review.service.dto.CreateReportRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class ReviewReportController {

    private final ReviewReportService reviewReportService;

    @PostMapping("/reviews/{reviewId}/reports")
    public ResponseEntity<ApiResponse<Void>> createReport(
        @PathVariable Long reviewId,
        @RequestParam Long reporterId,
        @RequestBody CreateReportRequest dto
    ) {
        reviewReportService.createReviewReport(reviewId, reporterId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success());
    }

    @PatchMapping("/admin/reports/{reportId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveReport(
        @PathVariable Long reportId,
        @RequestParam Long adminId
    ) {
        reviewReportService.approveReport(reportId, adminId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PatchMapping("/admin/reports/{reportId}/reject")
    public ResponseEntity<ApiResponse<Void>> rejectReport(
        @PathVariable Long reportId,
        @RequestParam Long adminId) {
        reviewReportService.rejectReport(reportId, adminId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
