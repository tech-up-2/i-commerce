package com.example.i_commerce.domain.review.service.dto;

import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.ReviewImage;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long userId;

    private String content;

    private Integer starRate;

    private List<String> imageUrls;

    private boolean isBest;

    private LocalDateTime createdAt;

    private boolean isUpdated;

    public static ReviewResponse from(Review review) {

        List<String> extractedUrls = review.getImages() != null ?
            review.getImages().stream()
                .map(ReviewImage::getImageUrl)
                .toList()
            : Collections.emptyList();

        return ReviewResponse.builder()
            .userId(review.getUserId())
            .content(review.getContent())
            .starRate(review.getStarRate())
            .imageUrls(extractedUrls)
            .isBest(Boolean.TRUE.equals(review.getIsBest()))
            .createdAt(review.getCreatedAt())
            .isUpdated(Boolean.TRUE.equals(review.getIsUpdated()))
            .build();
    }

}
