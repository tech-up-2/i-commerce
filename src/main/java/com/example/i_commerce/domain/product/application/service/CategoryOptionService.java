package com.example.i_commerce.domain.product.application.service;


import com.example.i_commerce.domain.product.application.dto.AlreadyExistsOption;
import com.example.i_commerce.domain.product.application.dto.CategoryOptionDto;
import com.example.i_commerce.domain.product.controller.request.AddCategoryOptionRequest;
import com.example.i_commerce.domain.product.controller.response.AddCategoryOptionResponse;
import com.example.i_commerce.domain.product.controller.response.CategoryOptionResponse;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.CategoryOption;
import com.example.i_commerce.domain.product.entity.Option;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.CategoryOptionRepository;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.domain.product.repository.OptionRepository;
import com.example.i_commerce.domain.product.repository.projection.CategoryOptionKey;
import com.example.i_commerce.domain.product.repository.projection.CategoryOptionProjection;
import com.example.i_commerce.global.exception.AppException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryOptionService {

    private final CategoryRepository categoryRepository;
    private final OptionRepository optionRepository;
    private final CategoryOptionRepository categoryOptionRepository;

    @Transactional(readOnly = true)
    public CategoryOptionResponse getOptionsByCategory(Long categoryId) {

        if(!categoryRepository.existsById(categoryId)) {
            throw new AppException(ProductErrorCode.CATEGORY_NOT_FOUND);
        }

        List<CategoryOptionProjection> projections =
            categoryOptionRepository.findOptionsByCategoryId(categoryId);

        List<CategoryOptionDto> options = projections.stream()
            .map(CategoryOptionDto::from)
            .toList();

        return CategoryOptionResponse.of(categoryId, options);
    }

    @Transactional
    public AddCategoryOptionResponse addCategoryOptions(
        Long categoryId,
        AddCategoryOptionRequest request
    ) {

        if(!categoryRepository.existsById(categoryId)) {
            throw new AppException(ProductErrorCode.CATEGORY_NOT_FOUND);
        }

        List<Option> options = optionRepository.findAllById(request.optionIds());
        if(request.optionIds().size() != options.size()) {
            throw new AppException(ProductErrorCode.OPTION_NOT_FOUND);
        }

        List<Long> targetCategoryIds = (request.propagateToChildren())
            ? categoryRepository.findAllDescendantIds(categoryId)
            : List.of(categoryId);

        Set<CategoryOptionKey> existingKey = new HashSet<>(
            categoryOptionRepository.findExistingKeys(targetCategoryIds, request.optionIds())
        );

        List<CategoryOption> categoryOptions = new ArrayList<>();
        List<AlreadyExistsOption> existsOptions = new ArrayList<>();

        for(Long targetCategoryId : targetCategoryIds) {
            Category categoryRef = categoryRepository.getReferenceById(targetCategoryId);
            for(Option option : options) {
                if(existingKey.contains(new CategoryOptionKey(targetCategoryId, option.getId()))) {
                    existsOptions.add(AlreadyExistsOption.of(
                        targetCategoryId, option.getId()
                    ));
                    continue;
                }
                categoryOptions.add(CategoryOption.of(
                    categoryRef,
                    option,
                    request.required()
                ));
            }
        }

        categoryOptionRepository.saveAll(categoryOptions);

        return AddCategoryOptionResponse.of(categoryId, existsOptions);
    }

}
