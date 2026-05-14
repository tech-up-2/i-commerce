package com.example.i_commerce.domain.review.controller;


import com.example.i_commerce.domain.review.facade.ReviewLikeFacade;
import com.example.i_commerce.global.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewLikeController {

    private final ReviewLikeFacade reviewLikeFacade;

    @PostMapping("/{reviewId}/likes")
    public ResponseEntity<ApiResponse<Boolean>> toggleLike(
        @PathVariable("reviewId") Long reviewId,
        @RequestParam("likerId") Long likerId) throws InterruptedException {

        boolean isLiked = reviewLikeFacade.toggleLikeWithRetry(reviewId, likerId);
        return ResponseEntity.ok(ApiResponse.success(isLiked));
    }

    @PostMapping("/{reviewId}/best")
    public ResponseEntity<ApiResponse<Void>> approveBestReview(@PathVariable("reviewId") Long reviewId) {
        reviewLikeFacade.approveBestReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @DeleteMapping("/{reviewId}/best")
    public ResponseEntity<ApiResponse<Void>> cancelBestReview(@PathVariable("reviewId") Long reviewId) {
        reviewLikeFacade.cancelBestReview(reviewId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/{reviewId}/exclude")
    public ResponseEntity<ApiResponse<Void>> excludeFromBest(@PathVariable("reviewId") Long reviewId) {
        reviewLikeFacade.excludeFromBest(reviewId);
        return ResponseEntity.ok(ApiResponse.success());
    }

}
