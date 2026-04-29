package com.example.i_commerce.domain.review.service.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class UpdateReviewRequest {

    private Long userId;

    private String content;

    private Integer starRate;

    private String imageUrl;

    private LocalDateTime updatedAt;

}
