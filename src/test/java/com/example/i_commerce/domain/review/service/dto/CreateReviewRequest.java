package com.example.i_commerce.domain.review.service.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateReviewRequest {
    private Integer StarRate;
    private String content;
}
