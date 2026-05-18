package com.example.i_commerce.domain.review.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(name = "UpdateCommentRequest", description = "리뷰 답글 수정")
@Getter
@AllArgsConstructor
@Builder
public class UpdateCommentRequest {

    private String content;
}
