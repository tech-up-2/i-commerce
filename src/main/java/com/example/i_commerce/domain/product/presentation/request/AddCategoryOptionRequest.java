package com.example.i_commerce.domain.product.presentation.request;

import com.example.i_commerce.global.validation.annotations.NoDuplicates;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record AddCategoryOptionRequest(
    @NotEmpty
    @NoDuplicates
    List<Long> optionIds,

    @NotNull
    Boolean propagateToChildren,

    @NotNull
    Boolean required
) {

}
