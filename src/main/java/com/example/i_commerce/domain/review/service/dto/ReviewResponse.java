package com.example.i_commerce.domain.review.service.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long userId;

    private String content;

    private Integer score;

    private String imageUrl;

    private boolean isBest;

    private LocalDateTime createdAt;

    private boolean isUpdated;

}
