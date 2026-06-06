package com.example.i_commerce.domain.review.service.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateReviewRequest {
    private Long userId;
    private String content;
    private Integer starRate;
    private List<String> imageUrls;
}
