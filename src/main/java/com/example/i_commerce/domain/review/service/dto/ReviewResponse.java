package com.example.i_commerce.domain.review.service.dto;

import com.example.i_commerce.domain.review.entity.Review;
import com.example.i_commerce.domain.review.entity.ReviewImage;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(name = "ReviewResponse", description = "리뷰 조회 응답")
@Getter
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long userId;

    private String content;

    private Integer starRate;

    private List<String> imageUrls;

    private boolean isBest;

    private Long likeCount;

    private LocalDateTime createdAt;

    private boolean isUpdated;

    private List<CommentResponse> comments;

    public static ReviewResponse from(Review review) {

        List<String> extractedUrls = review.getImages() != null ?
            review.getImages().stream()
                .map(ReviewImage::getImageUrl)
                .toList()
            : Collections.emptyList();

        return ReviewResponse.builder()
            .reviewId(review.getId())
            .userId(review.getUserId())
            .content(review.getContent())
            .starRate(review.getStarRate())
            .imageUrls(extractedUrls)
            .isBest(Boolean.TRUE.equals(review.getIsBest()))
            .likeCount(review.getLikeCount())
            .createdAt(review.getCreatedAt())
            .isUpdated(Boolean.TRUE.equals(review.getIsUpdated()))
            .comments(Collections.emptyList())
            .build();
    }

    public static ReviewResponse of(Review review, List<CommentResponse> comments) {

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
            .likeCount(review.getLikeCount())
            .createdAt(review.getCreatedAt())
            .isUpdated(Boolean.TRUE.equals(review.getIsUpdated()))
            .comments(comments)
            .build();
    }

}
