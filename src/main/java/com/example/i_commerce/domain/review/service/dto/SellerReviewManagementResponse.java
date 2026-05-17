package com.example.i_commerce.domain.review.service.dto;


import com.example.i_commerce.domain.review.entity.Review;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(name = "SellerReviewManagementResponse", description = "판매자 리뷰 관리 응답")
@Getter
@AllArgsConstructor
@Builder
public class SellerReviewManagementResponse {
    private Long reviewId;
    private String productName;
    private String customerName;
    private Integer starRate;
    private String reviewContent;
    private LocalDateTime reviewCreatedAt;

    private boolean hasComment;
    private Long commentId;
    private String commentContent;

    public SellerReviewManagementResponse(
        Long reviewId, String productName, String customerName,
        Integer starRate, String reviewContent, LocalDateTime reviewCreatedAt,
        Long commentId, String commentContent
    ) {
        this.reviewId = reviewId;
        this.productName = productName;
        this.customerName = customerName;
        this.starRate = starRate;
        this.reviewContent = reviewContent;
        this.reviewCreatedAt = reviewCreatedAt;
        this.commentId = commentId;
        this.commentContent = commentContent;
        this.hasComment = (commentId != null);
    }

}
