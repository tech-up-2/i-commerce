package com.example.i_commerce.domain.review.controller;

import com.example.i_commerce.domain.review.service.ReviewService;
import com.example.i_commerce.domain.review.service.dto.CreateReviewRequest;
import com.example.i_commerce.global.common.response.ApiResponse;
import com.example.i_commerce.global.security.principal.CustomUserPrincipal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "orderProduct Review API", description = "상품 리뷰 관련 API")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequiredArgsConstructor
@Validated
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
        @RequestPart(value = "review") @Valid CreateReviewRequest request,

        @Size(max = 10, message = "이미지는 최대 10까지 업로드할 수 있습니다.")
        @RequestPart(value = "images", required = false)
        List<MultipartFile> imageFiles
    ) {
        Long createdReviewId = reviewService.createReview(orderProductId, principal.getId(), request, imageFiles);
        return ApiResponse.success(createdReviewId);
    }


}
