package com.example.i_commerce.domain.product.application.validator;


import com.example.i_commerce.domain.product.presentation.request.CreateProductRequest.OptionRequest;
import com.example.i_commerce.domain.product.entity.CategoryOption;
import com.example.i_commerce.domain.product.entity.Option;
import com.example.i_commerce.domain.product.entity.enums.ProductOptionType;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.CategoryOptionRepository;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductOptionValidator {

    private static final int MAX_OPTION_COUNT = 2;
    private final CategoryOptionRepository categoryOptionRepository;

    public void validateOptions(
        Long categoryId,
        ProductOptionType productOptionType,
        List<OptionRequest> options
    ) {

        if(productOptionType == ProductOptionType.NONE) {
            return;
        }

        if (options.size() > MAX_OPTION_COUNT) {
            throw new AppException(ProductErrorCode.EXCEEDED_MAX_OPTION);
        }

        Set<Long> availableOptions = categoryOptionRepository
            .findAllByCategoryId(categoryId)
            .stream()
            .map(CategoryOption::getOption)
            .map(Option::getId)
            .collect(Collectors.toSet());

        List<Long> invalidOptionIds = options.stream()
            .map(OptionRequest::optionId)
            .filter(id -> !availableOptions.contains(id))
            .toList();

        if(!invalidOptionIds.isEmpty()) {
            throw new AppException(ProductErrorCode.NOT_SUPPORTED_OPTION);
        }

    }

}
