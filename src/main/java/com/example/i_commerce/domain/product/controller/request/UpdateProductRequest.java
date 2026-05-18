package com.example.i_commerce.domain.product.controller.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record UpdateProductRequest(
    @NotBlank(message = "상품명은 필수입니다.")
    @Size(max = 100, message = "상품명은 100자 이하여야 합니다.")
    String name,

    @Size(max = 1000, message = "상품 설명은 1000자 이하여야 합니다.")
    String description
) {

}
