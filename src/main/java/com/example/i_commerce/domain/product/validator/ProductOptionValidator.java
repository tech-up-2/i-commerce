package com.example.i_commerce.domain.product.validator;


import com.example.i_commerce.domain.product.controller.request.CreateProductRequest;
import com.example.i_commerce.domain.product.controller.request.CreateProductRequest.OptionRequest;
import com.example.i_commerce.domain.product.controller.request.CreateProductRequest.OptionValueRequest;
import com.example.i_commerce.domain.product.entity.CategoryOption;
import com.example.i_commerce.domain.product.entity.Option;
import com.example.i_commerce.domain.product.enums.OptionType;
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

    public void validateOptions(CreateProductRequest request) {

        if(request.optionType() == OptionType.NONE) {
            return;
        }

        List<OptionRequest> requests = request.options();

        Set<Long> availableOptions = categoryOptionRepository
            .findByCategoryId(request.categoryId())
            .stream()
            .map(CategoryOption::getOption)
            .map(Option::getId)
            .collect(Collectors.toSet());

        List<Long> invalidOptionIds = requests.stream()
            .map(OptionRequest::optionId)
            .filter(id -> !availableOptions.contains(id))
            .toList();

        if(!invalidOptionIds.isEmpty()) {
            throw new AppException(ProductErrorCode.INVALID_OPTION);
        }

    }

    private void validateOptionCount(List<OptionValueRequest> options) {
        if (options.size() > MAX_OPTION_COUNT) {
            throw new AppException(ProductErrorCode.EXCEEDED_MAX_OPTION);
        }
    }

}
