package com.example.i_commerce.domain.review.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(name = "CreateReviewRequest", description = "리뷰 생성 요청")
@Getter
@AllArgsConstructor
@Builder
public class CreateReviewRequest {

    private String content;

    private Integer starRate;

    private List<String> imageUrls;

}
