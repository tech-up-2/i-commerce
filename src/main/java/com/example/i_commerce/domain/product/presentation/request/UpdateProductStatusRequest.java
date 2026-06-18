package com.example.i_commerce.domain.product.presentation.request;

import com.example.i_commerce.domain.product.entity.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateProductStatusRequest(
    @NotNull(message = "변경할 상태는 필수입니다.")
    ProductStatus status
) {

}
