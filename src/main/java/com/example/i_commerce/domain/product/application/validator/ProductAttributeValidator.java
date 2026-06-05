package com.example.i_commerce.domain.product.application.validator;


import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest.ItemAttributeRequest;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest.ProductItemRequest;
import com.example.i_commerce.domain.product.entity.Attribute;
import com.example.i_commerce.domain.product.entity.CategoryAttribute;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.AttributeRepository;
import com.example.i_commerce.domain.product.repository.CategoryAttributeRepository;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class ProductAttributeValidator {

    private final AttributeRepository attributeRepository;
    private final CategoryAttributeRepository categoryAttributeRepository;

    public Map<Long, Attribute> validateAndFetchAttributes(
        Long categoryId,
        List<ProductItemRequest> items
    ) {

        Set<Long> requestedAttributeIds = items.stream()
            .filter(item -> item.attributes() != null)
            .flatMap(item -> item.attributes().stream())
            .map(ItemAttributeRequest::attributeId)
            .collect(Collectors.toSet());

        if (requestedAttributeIds.isEmpty()) {
            return Map.of();
        }

        List<CategoryAttribute> categoryAttributes =
            categoryAttributeRepository.findByCategoryIdWithAttribute(categoryId);

        Set<Long> supportedAttributeIds = categoryAttributes.stream()
            .map(ca -> ca.getAttribute().getId())
            .collect(Collectors.toSet());

        Set<Long> unsupportedAttributes = requestedAttributeIds.stream()
            .filter(id -> !supportedAttributeIds.contains(id))
            .collect(Collectors.toSet());

        if (!unsupportedAttributes.isEmpty()) {
            throw new AppException(ProductErrorCode.NOT_SUPPORTED_ATTRIBUTE);
        }

        return fetchAttributeMap(requestedAttributeIds);
    }

    private Map<Long, Attribute> fetchAttributeMap(Set<Long> attributeIds) {
        if (attributeIds.isEmpty()) {
            return Map.of();
        }

        return attributeRepository.findAllById(attributeIds)
            .stream()
            .collect(Collectors.toMap(Attribute::getId, Function.identity()));
    }

}
