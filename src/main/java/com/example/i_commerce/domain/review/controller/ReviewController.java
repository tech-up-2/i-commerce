package com.example.i_commerce.domain.review.controller;

import com.example.i_commerce.domain.review.service.ReviewService;
import com.example.i_commerce.domain.review.service.dto.CreateReviewRequest;
import com.example.i_commerce.domain.review.service.dto.DeleteReviewRequest;
import com.example.i_commerce.domain.review.service.dto.ReviewListResponse;
import com.example.i_commerce.domain.review.service.dto.ReviewResponse;
import com.example.i_commerce.domain.review.service.dto.UpdateReviewRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Review API", description = "리뷰 관련 API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 생성", description = "리뷰를 생성한다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createReview(
        @RequestBody CreateReviewRequest dto) {
        Long createdReviewId = reviewService.createReview(dto);
        return ResponseEntity.ok(ApiResponse.success(createdReviewId));
    }

    @Operation(summary = "리뷰 목록 불러오기", description = "리뷰 목록을 불러온다.")
    @GetMapping("/{productOrderId}")
    public ResponseEntity<ApiResponse<List<ReviewListResponse>>> viewReviewList(
        @PathVariable Long productOrderId) {
        List<ReviewListResponse> responses = reviewService.viewReviewList(productOrderId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "리뷰 보기", description = "리뷰를 본다.")
    @GetMapping("/{productOrderId}/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> viewDetailReview(
        @PathVariable Long reviewId) {
        ReviewResponse responseDto = reviewService.viewDetailReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @Operation(summary = "리뷰 수정", description = "리뷰를 수정한다.")
    @PatchMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Long>> editReview(
        @PathVariable Long reviewId,
        @RequestBody UpdateReviewRequest dto) {
        reviewService.editReview(reviewId, dto);
        return ResponseEntity.ok(ApiResponse.success(reviewId));
    }

    @Operation(summary = "리뷰 삭제", description = "리뷰를 삭제한다.")
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
        @PathVariable Long reviewId,
        @RequestBody DeleteReviewRequest dto) {
        reviewService.deleteReview(dto.getUserId(), reviewId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @Operation(summary = "베스트 리뷰 불러오기", description = "베스트 리뷰를 불러온다.")
    @GetMapping("/best-candidates")
    public ResponseEntity<ApiResponse<List<ReviewListResponse>>> getBestReviewCandidates(
        @RequestParam Long productOrderId
    ) {
        List<ReviewListResponse> responses = reviewService.getBestReviewCandidates(productOrderId);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
