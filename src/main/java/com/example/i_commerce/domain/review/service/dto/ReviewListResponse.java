package com.example.i_commerce.domain.review.service.dto;

import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.enums.ReviewIsBestStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(name = "ReviewListResponse", description = "리뷰 목록 조회 응답")
@Getter
@AllArgsConstructor
@Builder
public class ReviewListResponse {

    private Long reviewId;

    private Long userId;

    private String content;

    private Integer starRate;

    private boolean isBest;

    private String firstImageUrl;

    private Long likeCount;

    private ReviewIsBestStatus bestStatus;

    public static ReviewListResponse from(Review review) {

        String firstUrl = (review.getImages() != null && !review.getImages().isEmpty())
            ? review.getImages().get(0).getImageUrl()
            : null;

        return ReviewListResponse.builder()
            .reviewId(review.getId())
            .userId(review.getUserId())
            .content(review.getContent())
            .starRate(review.getStarRate())
            .likeCount(review.getLikeCount())
            .firstImageUrl(firstUrl)
            .isBest(review.getIsBest())
            .build();
    }

    public static ReviewListResponse ofCandidate(Review review) {
        String firstUrl = (review.getImages() != null && !review.getImages().isEmpty())
            ? review.getImages().get(0).getImageUrl()
            : null;

        return ReviewListResponse.builder()
            .reviewId(review.getId())
            .userId(review.getUserId())
            .content(review.getContent())
            .starRate(review.getStarRate())
            .likeCount(review.getLikeCount())
            .firstImageUrl(firstUrl)
            .bestStatus(ReviewIsBestStatus.BEST)
            .isBest(review.getIsBest())
            .build();
    }
}
