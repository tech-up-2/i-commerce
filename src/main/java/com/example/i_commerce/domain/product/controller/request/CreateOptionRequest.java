package com.example.i_commerce.domain.product.controller.request;

import com.example.i_commerce.domain.product.entity.OptionInputType;
import com.example.i_commerce.global.validation.annotations.NoDuplicates;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateOptionRequest(
    @NotBlank(message = "옵션 타입명은 필수입니다.")
    String type,

    @NotNull(message = "입력 방식은 필수입니다.")
    OptionInputType inputType,

    @NotEmpty(message = "옵션 값이 필요합니다.")
    @NoDuplicates(message = "옵션 값은 중복될 수 없습니다.")
    List<String> values
) {

}
