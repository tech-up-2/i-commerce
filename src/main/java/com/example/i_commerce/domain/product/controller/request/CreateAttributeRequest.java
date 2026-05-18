package com.example.i_commerce.domain.product.controller.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CreateAttributeRequest(

    @NotBlank(message = "속성 키는 필수입니다.")
    String key,

    @NotEmpty(message = "속성 값이 필요합니다.")
    List<@NotBlank(message = "속성 값은 공백일 수 없습니다.")String> values

) {

}
