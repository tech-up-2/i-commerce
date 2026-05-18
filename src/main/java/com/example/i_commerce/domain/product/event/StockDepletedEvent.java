package com.example.i_commerce.domain.product.event;

import java.util.List;

public record StockDepletedEvent(
    List<Long> productItemIds
) {

}
