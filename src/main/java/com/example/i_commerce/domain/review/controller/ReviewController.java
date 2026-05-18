package com.example.i_commerce.domain.review.controller;

import com.example.i_commerce.domain.review.facade.ReviewLikeFacade;
import com.example.i_commerce.domain.review.service.ReviewReportService;
import com.example.i_commerce.domain.review.service.ReviewService;
import com.example.i_commerce.domain.review.service.dto.CreateReportRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final ReviewLikeFacade reviewLikeFacade;
    private final ReviewReportService reviewReportService;

    @Operation(summary = "리뷰 목록 보기", description = "특정 상품에 대한 전체 리뷰를 본다.")
    @GetMapping
    public ApiResponse<List<ReviewListResponse>> viewReviewList(
        @RequestParam Long productId
    ) {
        List<ReviewListResponse> responses = reviewService.viewReviewList(productId);
        return ApiResponse.success(responses);
    }

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
        Long editedReviewId = reviewService.editReview(reviewId, principal.getId(), dto);
        return ApiResponse.success(editedReviewId);
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

    @Operation(summary = "좋아요 생성", description = "사용자가 좋아요를 한 번 누르면 생성, 두 번 누르면 취소된다.")
    @PreAuthorize("@authChecker.canLike()")
    @PostMapping("/{reviewId}/likes")
    public ApiResponse<Boolean> toggleLike(
        @PathVariable Long reviewId,
        @AuthenticationPrincipal CustomUserPrincipal principal) throws InterruptedException {

        boolean isLiked = reviewLikeFacade.toggleLikeWithRetry(reviewId, principal.getId());
        return ApiResponse.success(isLiked);
    }

    @Operation(summary = "리뷰 신고", description = "사용자가 특정 리뷰를 신고할 때 사용한다.")
    @PostMapping("/{reviewId}/reports")
    @PreAuthorize("@authChecker.canReportReview()")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> createReport(
        @PathVariable Long reviewId,
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody @Valid CreateReportRequest dto
    ) {
        reviewReportService.createReviewReport(reviewId, principal.getId(), dto);
        return ApiResponse.success();
    }

}


