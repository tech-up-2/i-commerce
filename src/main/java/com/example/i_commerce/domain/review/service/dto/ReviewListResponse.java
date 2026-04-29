package com.example.i_commerce.domain.review.service.dto;

import com.example.i_commerce.domain.review.entity.Review;
import java.util.List;
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

    private String firstImageUrl;

    public static ReviewListResponse from(Review review) {

        String firstUrl = (review.getImages() != null && !review.getImages().isEmpty())
            ? review.getImages().get(0).getImageUrl()
            : null;

        return ReviewListResponse.builder()
            .reviewId(review.getId())
            .userId(review.getUserId())
            .content(review.getContent())
            .starRate(review.getStarRate())
            .firstImageUrl(firstUrl)
            .isBest(review.getIsBest())
            .build();
    }
}
