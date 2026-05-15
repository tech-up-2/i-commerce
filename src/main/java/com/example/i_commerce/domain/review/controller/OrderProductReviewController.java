package com.example.i_commerce.domain.review.controller;

import com.example.i_commerce.domain.review.service.ReviewService;
import com.example.i_commerce.domain.review.service.dto.CreateReviewRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "orderProduct Review API", description = "상품 리뷰 관련 API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order-products")
public class OrderProductReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 생성", description = "특정 상품에 대한 리뷰를 생성한다.")
    @PreAuthorize("@authChecker.canWriteReviewAsMember()")
    @PostMapping("/{orderProductId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> createReview(
        @PathVariable Long orderProductId,
        @AuthenticationPrincipal CustomUserPrincipal principal,
        @RequestBody @Valid CreateReviewRequest request
    ) {
        Long createdReviewId = reviewService.createReview(orderProductId, principal.getId(), request);
        return ApiResponse.success(createdReviewId);
    }


}
