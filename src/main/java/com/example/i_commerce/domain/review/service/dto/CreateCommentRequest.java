package com.example.i_commerce.domain.review.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Schema(name = "CreateCommentRequest", description = "리뷰 답글 생성 요청")
@Getter
@AllArgsConstructor
@Builder
public class CreateCommentRequest {

    private String content;

    private Long parentId;

}
