package com.example.i_commerce.domain.review.controller;

import com.example.i_commerce.domain.review.service.ReviewService;
import com.example.i_commerce.domain.review.service.dto.ReviewListResponse;
import com.example.i_commerce.domain.review.service.dto.ReviewResponse;
import com.example.i_commerce.domain.review.service.dto.UpdateReviewRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Review API", description = "리뷰 관련 API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 상세 보기", description = "상세 리뷰를 본다.")
    @GetMapping("/{reviewId}")
    public ApiResponse<ReviewResponse> viewDetailReview(
        @PathVariable Long reviewId) {
        ReviewResponse response = reviewService.viewDetailReview(reviewId);
        return ApiResponse.success(response);
    }

    @Operation(summary = "리뷰 수정", description = "리뷰를 수정한다.")
    @PreAuthorize("@authChecker.canWriteReviewAsMember()")
    @PatchMapping("/{reviewId}")
    public ApiResponse<Long> editReview(
        @PathVariable Long reviewId,
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody @Valid UpdateReviewRequest dto) {
        reviewService.editReview(reviewId, principal.getId(), dto);
        return ApiResponse.success(reviewId);
    }

    @Operation(summary = "리뷰 삭제", description = "리뷰를 삭제한다.")
    @PreAuthorize("@authChecker.canDeleteReview()")
    @DeleteMapping("/{reviewId}")
    public ApiResponse<Void> deleteReview(
        @PathVariable Long reviewId,
        @AuthenticationPrincipal CustomUserPrincipal principal) {
        reviewService.deleteReview(principal.getId(), reviewId);
        return ApiResponse.success();
    }

    @Operation(summary = "베스트 리뷰 후보 불러오기", description = "판매자는 베스트 리뷰 후보를 확인한다.")
    @PreAuthorize("@authChecker.canManageSellerReview()")
    @GetMapping("/best-candidates")
    public ApiResponse<List<ReviewListResponse>> getBestReviewCandidates(
        @RequestParam Long productOrderId
    ) {
        List<ReviewListResponse> responses = reviewService.getBestReviewCandidates(productOrderId);

        return ApiResponse.success(responses);
    }
}


