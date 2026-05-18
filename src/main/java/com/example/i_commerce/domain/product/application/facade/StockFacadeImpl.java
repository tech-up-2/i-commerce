package com.example.i_commerce.domain.product.application.facade;

import com.example.i_commerce.domain.product.facade.StockFacade;
import com.example.i_commerce.domain.product.facade.dto.StockDeductCommand;
import com.example.i_commerce.domain.product.application.service.StockService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class StockFacadeImpl implements StockFacade {

    private final StockService stockService;

    @Override
    public void deductStock(List<StockDeductCommand> commands) {
        stockService.deductStocks(commands);
    }

    @Override
    public void rollbackStocks(Long orderId) {
        stockService.rollbackStocks(orderId);
    }
}
