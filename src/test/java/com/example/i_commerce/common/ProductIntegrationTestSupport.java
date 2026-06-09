package com.example.i_commerce.common;

import com.example.i_commerce.domain.product.repository.AttributeRepository;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.domain.product.repository.ProductItemRepository;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.domain.product.repository.StockHistoryRepository;
import com.example.i_commerce.domain.product.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;

public class ProductIntegrationTestSupport extends IntegrationTestSupport {

    @Autowired
    protected StockRepository stockRepository;

    @Autowired
    protected StockHistoryRepository stockHistoryRepository;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected ProductItemRepository productItemRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected AttributeRepository attributeRepository;


    @AfterEach
    void tearDown() {
        stockHistoryRepository.deleteAllInBatch();
        stockRepository.deleteAllInBatch();
        productItemRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        attributeRepository.deleteAllInBatch();
    }


}
