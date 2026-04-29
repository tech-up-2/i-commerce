package com.example.i_commerce.domain.review.service.dto;

import com.example.i_commerce.domain.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ReviewListResponse {

    private Long reviewId;

    private Long userId;

    private String content;

    private Integer starRate;

    private boolean isBest;

    private String imageUrl;

    public static ReviewListResponse from(Review review) {
        return ReviewListResponse.builder()
            .reviewId(review.getId())
            .content(review.getContent())
            .starRate(review.getStarRate())
            .imageUrl(review.getImageUrl())
            .isBest(review.getIsBest())
            //.isUpdated(review.getIsUpdated())
            //.createdAt(review.getCreatedAt())
            .build();
    }
}
