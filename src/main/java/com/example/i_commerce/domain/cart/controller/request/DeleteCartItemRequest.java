package com.example.i_commerce.domain.cart.controller.request;


import com.example.i_commerce.global.validation.annotations.NoDuplicates;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record DeleteCartItemRequest(
    @NotEmpty
    @NoDuplicates(message = "중복된 상품이 있습니다.")
    List<@NotNull Long> cartItemIds
) {

}
