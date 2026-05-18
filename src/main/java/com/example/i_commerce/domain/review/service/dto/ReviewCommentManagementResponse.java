package com.example.i_commerce.domain.review.service.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(name = "ReviewCommentManagementResponse", description = "리뷰 답글 목록 조회")
@Getter
@AllArgsConstructor
@Builder
public class ReviewCommentManagementResponse {
    private Long reviewId;
    private String reviewContent;
    private String commentContent;
    private LocalDateTime reviewCreatedAt;
}
