package com.example.i_commerce.domain.product.facade;

import com.example.i_commerce.domain.product.facade.dto.StockDeductCommand;
import java.util.List;

public interface StockFacade {

    void deductStock(List<StockDeductCommand> commands);
    void rollbackStocks(Long orderId);

}
