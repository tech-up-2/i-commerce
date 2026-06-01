package com.example.i_commerce.domain.product.event;


import com.example.i_commerce.domain.product.application.dto.StockDeductCommand;
import java.util.List;

public record OrderCompletedEvent(
    List<StockDeductCommand> orderItems
) {
}
