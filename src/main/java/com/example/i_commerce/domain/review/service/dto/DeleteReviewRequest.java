package com.example.i_commerce.domain.review.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(name = "DeleteReviewRequest", description = "리뷰 삭제 요청")
@Getter
@AllArgsConstructor
@Builder
public class DeleteReviewRequest {

    private Long userId;

}
