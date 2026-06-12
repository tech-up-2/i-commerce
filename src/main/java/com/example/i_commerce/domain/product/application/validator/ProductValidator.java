package com.example.i_commerce.domain.product.application.validator;


import com.example.i_commerce.domain.product.entity.enums.ProductOptionType;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest.ItemAttributeRequest;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest.OptionRequest;
import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest.ProductItemRequest;
import com.example.i_commerce.domain.product.repository.CategoryAttributeRepository;
import com.example.i_commerce.domain.product.repository.CategoryOptionRepository;
import com.example.i_commerce.global.exception.AppException;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductValidator {

    private final CategoryOptionRepository categoryOptionRepository;
    private final CategoryAttributeRepository categoryAttributeRepository;

    public void validateOptions(
        Long categoryId,
        ProductOptionType productOptionType,
        List<OptionRequest> optionRequests
    ) {

        List<OptionRequest> requests = optionRequests != null
            ? optionRequests
            : List.of();

        productOptionType.validateOptionCount(requests.size());

        if (requests.isEmpty()) {
            return;
        }

        List<Long> optionIds = optionRequests.stream()
            .map(OptionRequest::optionId)
            .toList();

        if(new HashSet<>(optionIds).size() != optionIds.size()) {
            throw new AppException(ProductErrorCode.DUPLICATED_OPTION);
        }

        List<Long> categoryOptions = categoryOptionRepository
            .findAllIdsByCategoryIdAndOptionIds(categoryId, optionIds);

        if(categoryOptions.size() != optionIds.size()) {
            throw new AppException(ProductErrorCode.NOT_SUPPORTED_OPTION);
        }

    }

    public void validateAttributes(
        Long categoryId,
        List<ProductItemRequest> items
    ) {

        List<Long> requestedAttributeIds = items.stream()
            .filter(item -> item.attributes() != null)
            .flatMap(item -> item.attributes().stream())
            .map(ItemAttributeRequest::attributeId)
            .distinct()
            .toList();

        if (requestedAttributeIds.isEmpty()) {
            return;
        }

        List<Long> keys = categoryAttributeRepository.findAllAttributeIds(
            categoryId, requestedAttributeIds
        );

        if(requestedAttributeIds.size() != keys.size()) {
            throw new AppException(ProductErrorCode.NOT_SUPPORTED_ATTRIBUTE);
        }

    }

}
