package com.example.i_commerce.domain.product.validator;


import com.example.i_commerce.domain.product.controller.request.CreateProductRequest;
import com.example.i_commerce.domain.product.controller.request.CreateProductRequest.ItemAttributeRequest;
import com.example.i_commerce.domain.product.controller.request.CreateProductRequest.ProductItemRequest;
import com.example.i_commerce.domain.product.entity.Attribute;
import com.example.i_commerce.domain.product.entity.CategoryAttribute;
import com.example.i_commerce.domain.product.repository.AttributeRepository;
import com.example.i_commerce.domain.product.repository.CategoryAttributeRepository;
import com.example.i_commerce.global.error.AppException;
import com.example.i_commerce.global.error.ErrorCode;
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

    public Map<Long, Attribute> validateAndFetchAttributes(CreateProductRequest request) {
        Set<Long> requestedAttributeIds = collectRequestedAttributeIds(request.items());
        if (requestedAttributeIds.isEmpty()) {
            return Map.of();
        }
        Set<Long> supportedAttributeIds = fetchSupportedAttributeIds(request.categoryId());
        validateAttributesSupported(requestedAttributeIds, supportedAttributeIds);
        return fetchAttributeMap(requestedAttributeIds);
    }

    private Set<Long> collectRequestedAttributeIds(List<ProductItemRequest> items) {
        return items.stream()
            .filter(item -> item.attributes() != null)
            .flatMap(item -> item.attributes().stream())
            .map(ItemAttributeRequest::attributeId)
            .collect(Collectors.toSet());
    }

    private Set<Long> fetchSupportedAttributeIds(Long categoryId) {
        List<CategoryAttribute> categoryAttributes =
            categoryAttributeRepository.findByCategoryIdWithAttribute(categoryId);

        return categoryAttributes.stream()
            .map(ca -> ca.getAttribute().getId())
            .collect(Collectors.toSet());
    }

    private void validateAttributesSupported(
        Set<Long> requestedAttributeIds,
        Set<Long> supportedAttributeIds
    ) {
        Set<Long> unsupportedAttributes = requestedAttributeIds.stream()
            .filter(id -> !supportedAttributeIds.contains(id))
            .collect(Collectors.toSet());

        if (!unsupportedAttributes.isEmpty()) {
            throw new AppException(ErrorCode.NOT_SUPPORTED_ATTRIBUTE);
        }
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
