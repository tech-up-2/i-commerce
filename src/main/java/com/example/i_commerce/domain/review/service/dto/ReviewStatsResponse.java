package com.example.i_commerce.domain.review.service.dto;

import com.example.i_commerce.domain.review.repository.StarRateCountProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(name = "ReviewStatsResponse", description = "리뷰 별점 통계 응답")
@Getter
@AllArgsConstructor
@Builder
public class ReviewStatsResponse {

    private List<StarRateDetail> starDetails;

    private double averageStarRate;

    private long totalReviewCount;

    @Getter
    @AllArgsConstructor
    public static class StarRateDetail {
        private Integer starRate;
        private Long count;
    }
}
