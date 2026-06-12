package com.example.i_commerce.common;

import com.example.i_commerce.domain.member.repository.MemberRepository;
import com.example.i_commerce.domain.member.repository.SellerRepository;
import com.example.i_commerce.domain.member.repository.StoreAddressRepository;
import com.example.i_commerce.domain.member.repository.StoreRepository;
import com.example.i_commerce.domain.product.entity.Attribute;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.CategoryAttribute;
import com.example.i_commerce.domain.product.entity.CategoryOption;
import com.example.i_commerce.domain.product.entity.Option;
import com.example.i_commerce.domain.product.repository.AttributeRepository;
import com.example.i_commerce.domain.product.repository.CategoryAttributeRepository;
import com.example.i_commerce.domain.product.repository.CategoryOptionRepository;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.domain.product.repository.OptionRepository;
import com.example.i_commerce.domain.product.repository.ProductAttributeRepository;
import com.example.i_commerce.domain.product.repository.ProductImageRepository;
import com.example.i_commerce.domain.product.repository.ProductItemRepository;
import com.example.i_commerce.domain.product.repository.ProductOptionValueRepository;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.domain.product.repository.StockHistoryRepository;
import com.example.i_commerce.domain.product.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;


public class ProductIntegrationTestSupport extends IntegrationTestSupport {

    @Autowired
    protected StockRepository stockRepository;

    @Autowired
    protected StockHistoryRepository stockHistoryRepository;

    @Autowired
    protected ProductOptionValueRepository productOptionValueRepository;

    @Autowired
    protected ProductImageRepository productImageRepository;

    @Autowired
    protected ProductAttributeRepository productAttributeRepository;

    @Autowired
    protected ProductItemRepository productItemRepository;

    @Autowired
    protected ProductRepository productRepository;

    @Autowired
    protected CategoryOptionRepository categoryOptionRepository;

    @Autowired
    protected CategoryAttributeRepository categoryAttributeRepository;

    @Autowired
    protected CategoryRepository categoryRepository;

    @Autowired
    protected AttributeRepository attributeRepository;

    @Autowired
    protected OptionRepository optionRepository;

    @Autowired
    protected StoreAddressRepository storeAddressRepository;

    @Autowired
    protected StoreRepository storeRepository;

    @Autowired
    protected SellerRepository sellerRepository;

    @Autowired
    protected MemberRepository memberRepository;

    protected void mapCategoryOption(
        Category category, Option option
    ) {
        categoryOptionRepository.save(CategoryOption.builder()
            .category(category)
            .option(option)
            .required(false)
            .build());
    }

    protected void mapCategoryAttribute(
        Category category, Attribute attribute
    ) {
        categoryAttributeRepository.save(CategoryAttribute.builder()
            .category(category)
            .attribute(attribute)
            .required(false)
            .build());
    }

    @BeforeEach
    void tearDown() {
        stockHistoryRepository.deleteAllInBatch();
        stockRepository.deleteAllInBatch();
        productAttributeRepository.deleteAllInBatch();

        productItemRepository.deleteAllInBatch();
        productOptionValueRepository.deleteAllInBatch();
        productImageRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();

        categoryOptionRepository.deleteAllInBatch();
        categoryAttributeRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        optionRepository.deleteAllInBatch();
        attributeRepository.deleteAllInBatch();

        storeAddressRepository.deleteAllInBatch();
        storeRepository.deleteAllInBatch();
        sellerRepository.deleteAllInBatch();
        memberRepository.deleteAllInBatch();
    }


}
