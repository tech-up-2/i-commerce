package com.example.i_commerce.domain.product.application.helper;

import com.example.i_commerce.domain.product.entity.Attribute;
import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.ProductAttribute;
import com.example.i_commerce.domain.product.entity.ProductItem;
import com.example.i_commerce.domain.product.entity.ProductOptionValue;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest.ItemAttributeRequest;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest.OptionRequest;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest.OptionValueRequest;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest.ProductItemRequest;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class ProductAssembler {
    private static final int FIRST_OPTION_INDEX = 0;
    private static final int SECOND_OPTION_INDEX = 1;

    public OptionValueMapper assembleOptions(
        Product product,
        List<OptionRequest> optionRequests
    ) {
        OptionValueMapper mapper = OptionValueMapper.create();

        for (OptionRequest option : optionRequests) {
            for (OptionValueRequest value : option.values()) {
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

    public void assembleItems(
        Product product,
        List<ProductItemRequest> itemRequests,
        OptionValueMapper optionMapper,
        Map<Long, Attribute> attributeMap
    ) {
        for (ProductItemRequest itemReq : itemRequests) {
            ProductOptionValue pov1 = resolveOptionValue(optionMapper, itemReq, FIRST_OPTION_INDEX);
            ProductOptionValue pov2 = resolveOptionValue(optionMapper, itemReq, SECOND_OPTION_INDEX);

            ProductItem productItem = ProductItem.of(
                itemReq.sku(), itemReq.price(), itemReq.displayName(),
                pov1, pov2, itemReq.isDefault()
            );

            productItem.initStock(itemReq.stock());
            attachAttributes(productItem, itemReq.attributes(), attributeMap);
            product.addItem(productItem);
        }
    }

    private ProductOptionValue resolveOptionValue(
        OptionValueMapper mapper,
        ProductItemRequest itemReq,
        int index
    ) {
        if (itemReq.optionValues() == null || itemReq.optionValues().size() <= index) {
            return null;
        }
        String value = itemReq.optionValues().get(index);
        return mapper.getOrThrow(index + 1, value);
    }

    private void attachAttributes(
        ProductItem productItem,
        List<ItemAttributeRequest> attributeRequests,
        Map<Long, Attribute> attributeMap
    ) {
        if (attributeRequests == null || attributeRequests.isEmpty()) {
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
