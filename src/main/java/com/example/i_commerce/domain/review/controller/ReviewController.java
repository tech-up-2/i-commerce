package com.example.i_commerce.domain.review.controller;

import com.example.i_commerce.domain.review.service.ReviewService;
import com.example.i_commerce.domain.review.service.dto.CreateReviewRequest;
import com.example.i_commerce.domain.review.service.dto.DeleteReviewRequest;
import com.example.i_commerce.domain.review.service.dto.ReviewResponse;
import com.example.i_commerce.domain.review.service.dto.UpdateReviewRequest;
import com.example.i_commerce.domain.review.service.dto.ReviewListResponse;
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
import com.example.i_commerce.global.common.response.ApiResponse;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createReview(
        @RequestBody CreateReviewRequest dto) {
        Long createdReviewId = reviewService.createReview(dto);
        return ResponseEntity.ok(ApiResponse.success(createdReviewId));
    }

    @GetMapping("/{productOrderId}")
    public ResponseEntity<ApiResponse<List<ReviewListResponse>>> viewReviewList(
        @PathVariable Long productOrderId) {
        List<ReviewListResponse> responses = reviewService.viewReviewList(productOrderId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/{productOrderId}/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponse>> viewDetailReview(
        @PathVariable Long reviewId) {
        ReviewResponse responseDto = reviewService.viewDetailReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    @PatchMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Long>> editReview(
        @PathVariable Long reviewId,
        @RequestBody UpdateReviewRequest dto) {
        reviewService.editReview(reviewId, dto);
        return ResponseEntity.ok(ApiResponse.success(reviewId));
    }

    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
        @PathVariable Long reviewId,
        @RequestBody DeleteReviewRequest dto) {
        reviewService.deleteReview(dto.getUserId(), reviewId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/best-candidates")
    public ResponseEntity<ApiResponse<List<ReviewListResponse>>> getBestReviewCandidates(
        @RequestParam Long productOrderId
    ) {
        List<ReviewListResponse> responses = reviewService.getBestReviewCandidates(productOrderId);

        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
