package com.example.i_commerce.domain.review.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class DeleteReviewRequest {

    private Long userId;

}
