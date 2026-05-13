package com.example.i_commerce.domain.review.controller;

import com.example.i_commerce.domain.review.service.ReviewService;
import com.example.i_commerce.domain.review.service.dto.CreateReviewRequest;
import com.example.i_commerce.domain.review.service.dto.ReviewListResponse;
import com.example.i_commerce.global.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Review API", description = "리뷰 관련 API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/product-orders")
public class OrderProductReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 생성", description = "특정 상품에 대한 리뷰를 생성한다.")
    @PostMapping("/{orderProductId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> createReview(
        @PathVariable Long orderProductId,
        @RequestBody CreateReviewRequest request
    ) {
        Long createdReviewId = reviewService.createReview(orderProductId, request);
        return ApiResponse.success(createdReviewId);
    }

    @Operation(summary = "리뷰 목록 보기", description = "특정 상품에 대한 전체 리뷰를 본다.")
    @GetMapping("/{orderProductId}/reviews")
    public ApiResponse<List<ReviewListResponse>> viewReviewList(
        @PathVariable Long orderProductId
    ) {
        List<ReviewListResponse> responses = reviewService.viewReviewList(orderProductId);
        return ApiResponse.success(responses);
    }

}
