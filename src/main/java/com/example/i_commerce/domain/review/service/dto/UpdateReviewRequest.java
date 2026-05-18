package com.example.i_commerce.domain.review.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(name = "UpdateReviewRequest", description = "리뷰 수정 요청")
@Getter
@AllArgsConstructor
@Builder
public class UpdateReviewRequest {

    private Long userId;

    private String content;

    private Integer starRate;

    private List<String> imageUrls;

}
