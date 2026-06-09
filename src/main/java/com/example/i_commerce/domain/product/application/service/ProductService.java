package com.example.i_commerce.domain.product.application.service;


import com.example.i_commerce.domain.member.service.store.StoreService;
import com.example.i_commerce.domain.product.application.helper.ProductAssembler;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest;
import com.example.i_commerce.domain.product.presentation.response.CreatedProductResponse;
import com.example.i_commerce.domain.product.entity.Attribute;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.application.helper.OptionValueMapper;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.domain.product.application.validator.ProductAttributeValidator;
import com.example.i_commerce.domain.product.application.validator.ProductOptionValidator;
import com.example.i_commerce.global.exception.AppException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final StoreService storeService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    
    private final ProductAssembler productAssembler;
    private final ProductOptionValidator optionValidator;
    private final ProductAttributeValidator attributeValidator;


    @Transactional
    public CreatedProductResponse createProduct(Long userId, CreateProductRequest request) {

        if(!storeService.isStoreManager(userId, request.storeId())) {
            throw new AppException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
        }

        optionValidator.validateOptions(
            request.categoryId(), request.productOptionType(), request.options()
        );

        Map<Long, Attribute> attributeMap = attributeValidator
            .validateAndFetchAttributes(request.categoryId(), request.items());

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() ->  new AppException(ProductErrorCode.CATEGORY_NOT_FOUND));

        Product product = Product.of(
            request.storeId(),
            category,
            request.name(),
            request.description(),
            request.productOptionType()
        );

        OptionValueMapper optionMapper = productAssembler.assembleOptions(product, request.options());
        productAssembler.assembleItems(product, request.items(), optionMapper, attributeMap);
        Product saved = productRepository.save(product);

        return CreatedProductResponse.from(saved);
    }

}



