package com.example.i_commerce.domain.review.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(name = "SearchReviewRequest", description = "리뷰 검색")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchReviewRequest {
    @NotNull(message = "상품 ID는 필수입니다.")
    private Long productId;
    private String optionName;
    private String keyword;
    private Integer starRate;

}
