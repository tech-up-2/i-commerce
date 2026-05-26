package com.example.i_commerce.domain.product.event.listener;

import com.example.i_commerce.domain.product.application.dto.StockDeductCommand;
import com.example.i_commerce.domain.product.application.service.StockService;
import com.example.i_commerce.domain.product.event.OrderCancelledEvent;
import com.example.i_commerce.domain.product.event.OrderCompletedEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class StockEventListener {

    private final StockService stockService;

    @EventListener
    public void handleOrder(OrderCompletedEvent event) {
        List<StockDeductCommand> commands = event.orderItems().stream()
            .map(item -> StockDeductCommand.builder()
                .productItemId(item.productItemId())
                .quantity(item.quantity())
                .orderId(event.orderId())
                .build()
            ).toList();

        stockService.deductStocks(commands);
    }

    @EventListener
    public void handleOrderCancelled(OrderCancelledEvent event) {
        stockService.rollbackStocks(event.orderId());
    }

}
