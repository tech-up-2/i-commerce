package com.example.i_commerce.domain.product.service;


import com.example.i_commerce.domain.product.controller.request.CreateProductRequest;
import com.example.i_commerce.domain.product.controller.request.CreateProductRequest.ItemAttributeRequest;
import com.example.i_commerce.domain.product.controller.request.CreateProductRequest.OptionRequest;
import com.example.i_commerce.domain.product.controller.request.CreateProductRequest.OptionValueRequest;
import com.example.i_commerce.domain.product.controller.request.CreateProductRequest.ProductItemRequest;
import com.example.i_commerce.domain.product.controller.response.CreatedProductResponse;
import com.example.i_commerce.domain.product.entity.Attribute;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductAttribute;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.ProductOptionValue;
import com.example.i_commerce.domain.product.entity.service.OptionValueMapper;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.domain.product.validator.ProductAttributeValidator;
import com.example.i_commerce.domain.product.validator.ProductOptionValidator;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class ProductService {
    private static final int FIRST_OPTION_INDEX = 0;
    private static final int SECOND_OPTION_INDEX = 1;

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    private final ProductOptionValidator optionValidator;
    private final ProductAttributeValidator attributeValidator;


    @Transactional
    public CreatedProductResponse createProduct(Long sellerId, CreateProductRequest request) {

        optionValidator.validateOptions(request);
        Map<Long, Attribute> attributeMap = attributeValidator.validateAndFetchAttributes(request);

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() ->  new AppException(ProductErrorCode.CATEGORY_NOT_FOUND));

        Product product = productRepository.save(Product.of(
            request.storeId(),
            category,
            request.name(),
            request.description(),
            request.optionType().getCode()
        ));

        OptionValueMapper optionMapper = createProductOptions(product, request.options());
        createProductItem(product, request.items(), optionMapper, attributeMap);
        Product saved = productRepository.save(product);

        return CreatedProductResponse.from(saved);
    }


    private OptionValueMapper createProductOptions(
        Product product,
        List<OptionRequest> optionRequests
    ) {
        OptionValueMapper mapper = new OptionValueMapper();

        for(OptionRequest option : optionRequests) {
            for(OptionValueRequest value : option.values()) {
                ProductOptionValue optionValue = ProductOptionValue.of(
                    option.optionOrder(),
                    option.name(),
                    value.value(),
                    value.displayOrder()
                );
                product.addOptionValue(optionValue);
                mapper.put(option.optionOrder(), value.value(), optionValue);
            }
        }

        return mapper;
    }

    private void createProductItem(
        Product product,
        List<ProductItemRequest> itemRequests,
        OptionValueMapper optionMapper,
        Map<Long, Attribute> attributeMap
    ) {
        for (ProductItemRequest itemReq : itemRequests) {
            ProductOptionValue pov1 = getOptionValue(optionMapper, itemReq, FIRST_OPTION_INDEX);
            ProductOptionValue pov2 = getOptionValue(optionMapper, itemReq, SECOND_OPTION_INDEX);

            ProductItem productItem = ProductItem.of(
                itemReq.sku(), itemReq.price(), itemReq.displayName(),
                pov1, pov2, itemReq.isDefault()
            );

            productItem.initStock(itemReq.stock());
            addAttribute(productItem, itemReq.attributes(), attributeMap);
            product.addItem(productItem);
        }
    }


    private ProductOptionValue getOptionValue(
        OptionValueMapper mapper,
        ProductItemRequest itemReq,
        int index
    ) {
        if(itemReq.optionValues() == null || itemReq.optionValues().size() <= index) {
            return null;
        }
        String value = itemReq.optionValues().get(index);
        return mapper.get(index + 1, value);
    }

    private void addAttribute(
        ProductItem productItem,
        List<ItemAttributeRequest> attributeRequests,
        Map<Long, Attribute> attributeMap
    ) {
        if(attributeRequests == null || attributeRequests.isEmpty()) {
            return;
        }

        for (ItemAttributeRequest attReq : attributeRequests) {
            Attribute attribute = attributeMap.get(attReq.attributeId());

            if (attribute == null) {
                throw new AppException(ProductErrorCode.ATTRIBUTE_NOT_FOUND);
            }

            ProductAttribute productAttribute = ProductAttribute.of(
                attribute,
                attReq.displayName(),
                attReq.displayOrder()
            );

            productItem.addAttribute(productAttribute);
        }

    }

}






