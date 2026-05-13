package com.example.i_commerce.domain.review.controller;


import com.example.i_commerce.domain.review.facade.ReviewLikeFacade;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Review API", description = "리뷰 관련 API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews")
public class ReviewLikeController {

    private final ReviewLikeFacade reviewLikeFacade;

    @Operation(summary = "좋아요 생성", description = "사용자가 좋아요를 한 번 누르면 생성, 두 번 누르면 취소된다.")
    @PreAuthorize("@authChecker.canLike()")
    @PostMapping("/{reviewId}/likes")
    public ApiResponse<Boolean> toggleLike(
        @PathVariable Long reviewId,
        @AuthenticationPrincipal CustomUserPrincipal principal) throws InterruptedException {

        boolean isLiked = reviewLikeFacade.toggleLikeWithRetry(reviewId, principal.getId());
        return ApiResponse.success(isLiked);
    }

    @Operation(summary = "베스트 리뷰 선정", description = "판매자는 베스트 리뷰를 선정할 수 있다.")
    @PreAuthorize("@authChecker.canManageSellerReview()")
    @PostMapping("/{reviewId}/best")
    public ApiResponse<Void> approveBestReview(
        @PathVariable Long reviewId) {
        reviewLikeFacade.approveBestReview(reviewId);
        return ApiResponse.success();
    }

    @Operation(summary = "베스트 리뷰 취소", description = "판매자는 베스트 리뷰를 취소할 수 있다.")
    @PreAuthorize("@authChecker.canManageSellerReview()")
    @DeleteMapping("/{reviewId}/best")
    public ApiResponse<Void> cancelBestReview(
        @PathVariable Long reviewId) {
        reviewLikeFacade.cancelBestReview(reviewId);
        return ApiResponse.success();
    }

    @Operation(summary = "베스트 리뷰 후보 제외", description = "판매자는 추천된 베스트 리뷰 후보에서 특정 리뷰를 제외할 수 있다.")
    @PreAuthorize("@authChecker.canManageSellerReview()")
    @PostMapping("/{reviewId}/exclude")
    public ApiResponse<Void> excludeFromBest(
        @PathVariable Long reviewId) {
        reviewLikeFacade.excludeFromBest(reviewId);
        return ApiResponse.success();
    }

}
