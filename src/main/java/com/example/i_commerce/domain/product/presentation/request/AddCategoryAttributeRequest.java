package com.example.i_commerce.domain.product.presentation.request;


import com.example.i_commerce.global.validation.annotations.NoDuplicates;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;

@Schema(description = "카테고리가 제공할 속성 추가 요청")
public record AddCategoryAttributeRequest(

    @Schema(description = "추가할 속성 ID 목록", example = "[1, 2, 3]")
    @NotEmpty(message = "속성 ID 목록은 비어있을 수 없습니다.")
    @NoDuplicates(message = "속성 ID는 중복될 수 없습니다.")
    List<@Positive Long> attributeIds,

    @Schema(description = "하위 카테고리에도 추가할지 여부", example = "false")
    @NotNull
    Boolean propagateToChildren,

    @Schema(description = "필수 속성 여부", example = "false")
    @NotNull
    Boolean required
) {

}
