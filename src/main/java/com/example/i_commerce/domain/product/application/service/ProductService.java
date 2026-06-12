package com.example.i_commerce.domain.product.application.service;


import com.example.i_commerce.domain.member.service.store.StoreService;
import com.example.i_commerce.domain.product.application.validator.ProductValidator;
import com.example.i_commerce.domain.product.entity.ProductAttribute;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.ProductOptionValue;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest.ItemAttributeRequest;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest.ProductItemRequest;
import com.example.i_commerce.domain.product.presentation.response.CreatedProductResponse;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.application.helper.OptionValueMapper;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final StoreService storeService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ProductValidator productValidator;

    @Transactional
    public CreatedProductResponse createProduct(Long userId, CreateProductRequest request) {

        if(!storeService.isStoreManager(userId, request.storeId())) {
            throw new AppException(ProductErrorCode.PRODUCT_ACCESS_DENIED);
        }

        Long categoryId = request.categoryId();

        Category category = categoryRepository.findById(categoryId)
            .orElseThrow(() ->  new AppException(ProductErrorCode.CATEGORY_NOT_FOUND));

        productValidator.validateOptions(
            categoryId, request.productOptionType(), request.options()
        );
        productValidator.validateAttributes(categoryId, request.items());

        Product product = Product.of(
            request.storeId(),
            category,
            request.name(),
            request.description(),
            request.productOptionType()
        );

        OptionValueMapper optionMapper = OptionValueMapper.from(product, request.options());

        request.items().forEach(productItemRequest ->
            buildAndAttachItem(product, productItemRequest, optionMapper)
        );

        Product saved = productRepository.save(product);

        return CreatedProductResponse.from(saved);
    }

    private void buildAndAttachItem(
        Product product,
        ProductItemRequest itemReq,
        OptionValueMapper optionMapper
    ) {
        ProductOptionValue pov1 = resolveOptionValue(optionMapper, itemReq, 0);
        ProductOptionValue pov2 = resolveOptionValue(optionMapper, itemReq, 1);

        ProductItem productItem = ProductItem.of(
            itemReq.sku(),
            itemReq.price(),
            itemReq.displayName(),
            pov1,
            pov2,
            itemReq.isDefault()
        );

        productItem.initStock(itemReq.stock());
        attachAttributes(productItem, itemReq.attributes());
        product.addItem(productItem);
    }

    private ProductOptionValue resolveOptionValue(
        OptionValueMapper mapper,
        ProductItemRequest itemReq,
        int index
    ) {
        List<String> optionValues = itemReq.optionValues();
        if (optionValues == null || optionValues.size() <= index) {
            return null;
        }
        return mapper.getOrThrow(index + 1, optionValues.get(index));
    }

    private void attachAttributes(
        ProductItem productItem,
        List<ItemAttributeRequest> attributeRequests
    ) {
        if (attributeRequests == null || attributeRequests.isEmpty()) {
            return;
        }

        for(ItemAttributeRequest attReq : attributeRequests) {
            ProductAttribute productAttribute = ProductAttribute.of(
                attReq.attributeId(),
                attReq.displayName(),
                attReq.displayOrder()
            );
            productItem.addAttribute(productAttribute);
        }

    }

}


