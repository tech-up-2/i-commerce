package com.example.i_commerce.domain.review.controller;


import com.example.i_commerce.domain.review.facade.ReviewLikeFacade;
import com.example.i_commerce.domain.review.service.ReviewCommentService;
import com.example.i_commerce.domain.review.service.ReviewService;
import com.example.i_commerce.domain.review.service.dto.CreateCommentRequest;
import com.example.i_commerce.domain.review.service.dto.ReviewListResponse;
import com.example.i_commerce.domain.review.service.dto.SellerReviewManagementResponse;
import com.example.i_commerce.domain.review.service.dto.UpdateCommentRequest;
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
import com.example.i_commerce.global.common.response.SliceResponse;
import org.springframework.data.domain.Pageable;

@Tag(name = "Seller Review API", description = "판매자 리뷰 관리 API")
@SecurityRequirement(name = "BearerAuth")
@PreAuthorize("@authChecker.canManageSellerReview()")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/seller/reviews")
public class SellerReviewController {

    private final ReviewService reviewService;
    private final ReviewLikeFacade reviewLikeFacade;
    private final ReviewCommentService reviewCommentService;

    @Operation(summary = "베스트 리뷰 후보 조회", description = "판매자는 베스트 리뷰 후보를 확인한다.")
    @GetMapping("/best-candidates")
    public ApiResponse<List<ReviewListResponse>> getBestReviewCandidates(
        @RequestParam Long productId,
        @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        List<ReviewListResponse> responses = reviewService.getBestReviewCandidates(productId, principal.getId());

        return ApiResponse.success(responses);
    }

    @Operation(summary = "베스트 리뷰 후보 제외", description = "판매자는 추천된 베스트 리뷰 후보에서 특정 리뷰를 제외한다.")
    @PostMapping("/best-candidates/{reviewId}/exclude")
    public ApiResponse<Void> excludeFromBest(
        @PathVariable Long reviewId,
        @AuthenticationPrincipal CustomUserPrincipal principal) {
        reviewLikeFacade.excludeFromBest(reviewId, principal.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "베스트 리뷰 선정", description = "판매자는 베스트 리뷰를 선정한다.")
    @PostMapping("/{reviewId}/best")
    public ApiResponse<Void> approveBestReview(
        @PathVariable Long reviewId,
        @AuthenticationPrincipal CustomUserPrincipal principal) {
        reviewLikeFacade.approveBestReview(reviewId, principal.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "베스트 리뷰 선정 취소", description = "판매자는 베스트 리뷰 선정을 취소한다.")
    @DeleteMapping("/{reviewId}/best")
    public ApiResponse<Void> cancelBestReview(
        @PathVariable Long reviewId
    ,@AuthenticationPrincipal CustomUserPrincipal principal) {
        reviewLikeFacade.cancelBestReview(reviewId, principal.getId());
        return ApiResponse.success();
    }

    @Operation(summary = "리뷰 답글 생성", description = "판매자는 특정 리뷰에 답글을 한 번 달 수 있다.")
    @PostMapping("/{reviewId}/comment")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Void> createComment(
        @PathVariable Long reviewId,
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody @Valid CreateCommentRequest request) {
        reviewCommentService.createComment(reviewId, principal.getId(), request);
        return ApiResponse.success();
    }

    @Operation(summary = "리뷰 답글 수정", description = "판매자는 자신이 단 답글을 수정할 수 있다.")
    @PatchMapping("/comments/{commentId}")
    public ApiResponse<Long> editComment(
        @PathVariable Long commentId,
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody @Valid UpdateCommentRequest request
    ) {
        Long editedCommentId = reviewCommentService.editComment(commentId, principal.getId(), request);
        return ApiResponse.success(editedCommentId);
    }

    @Operation(summary = "판매자 상점 리뷰 목록 조회", description = "판매자가 운영하는 상점에 등록된 모든 상품 리뷰를 페이징하여 조회한다.")
    @GetMapping
    public ApiResponse<SliceResponse<ReviewListResponse>> getSellerReviews(
        @AuthenticationPrincipal CustomUserPrincipal principal,
        Pageable pageable
    ) {
        SliceResponse<ReviewListResponse> response = reviewCommentService.getSellerReviews(principal.getId(), pageable);
        return ApiResponse.success(response);
    }
}
