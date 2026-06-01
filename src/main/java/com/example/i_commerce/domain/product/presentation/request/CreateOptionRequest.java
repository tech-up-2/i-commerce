package com.example.i_commerce.domain.product.presentation.request;

import com.example.i_commerce.domain.product.entity.enums.OptionInputType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOptionRequest(
    @NotBlank(message = "옵션명은 필수입니다.")
    String name,

    @NotNull(message = "입력 방식은 필수입니다.")
    OptionInputType inputType
) {

}
