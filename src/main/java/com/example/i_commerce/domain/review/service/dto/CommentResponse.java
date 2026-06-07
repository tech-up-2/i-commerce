package com.example.i_commerce.domain.review.service.dto;

import com.example.i_commerce.domain.review.entity.ReviewComment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(name = "CommentResponse", description = "리뷰 답글 응답")
@Getter
@AllArgsConstructor
@Builder
public class CommentResponse {

    private Long commentId;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private boolean isUpdated;

    @Builder.Default
    private List<CommentResponse> children = new ArrayList<>();

    public static CommentResponse from(ReviewComment comment) {
        return CommentResponse.builder()
            .commentId(comment.getId())
            .userId(comment.getUserId())
            .content(comment.getContent())
            .createdAt(comment.getCreatedAt())
            .isUpdated(comment.getUpdatedAt() != null && !comment.getUpdatedAt().equals(comment.getCreatedAt()))
            .build();
    }
}