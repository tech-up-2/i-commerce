package com.example.i_commerce.domain.product.event.listener;

import com.example.i_commerce.domain.product.application.service.StockService;
import com.example.i_commerce.domain.product.event.OrderCancelledEvent;
import com.example.i_commerce.domain.product.event.OrderCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class StockEventListener {

    private final StockService stockService;

    @EventListener
    public void handleOrderCompleted(OrderCompletedEvent event) {
        if(event.orderItems().isEmpty()) {
            return;
        }

        stockService.deductStocks(event.orderItems());
    }

    @EventListener
    public void handleOrderCancelled(OrderCancelledEvent event) {
        stockService.rollbackStocks(event.orderId());
    }

}
