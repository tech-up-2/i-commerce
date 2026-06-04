package com.example.i_commerce.domain.product.presentation.request;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
    @Nullable
    Long parentId,

    @NotBlank(message = "카테고리 이름은 필수입니다.")
    String name
) {

}
