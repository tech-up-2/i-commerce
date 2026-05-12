package com.example.i_commerce.domain.cart.controller.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record AddCartItemRequest(
    @NotNull
    Long productItemId,

    @NotNull
    @Min(1)
    Integer quantity

) {
}
