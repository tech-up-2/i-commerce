package com.example.i_commerce.domain.product.event;


import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.ProductItemStatus;
import com.example.i_commerce.domain.product.repository.ProductItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProductItemEventHandler {
    private final ProductItemRepository productItemRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleStockDepleted(StockDepletedEvent event) {
        try {
            List<ProductItem> items = productItemRepository.findAllById(event.productItemIds());
            items.forEach(item -> item.changeStatus(ProductItemStatus.OUT_OF_STOCK));
        } catch (Exception e) {
            log.error("상품 상태 변경 실패. productItemIds={}", event.productItemIds(), e);
        }
    }
}
