package com.example.i_commerce.domain.product.application.service;


import com.example.i_commerce.domain.product.application.mapper.CategoryAttributeMapper;
import com.example.i_commerce.domain.product.controller.request.AddCategoryAttributeRequest;
import com.example.i_commerce.domain.product.controller.response.AddCategoryAttributeResponse;
import com.example.i_commerce.domain.product.controller.response.AddCategoryAttributeResponse.AlreadyExistsAttribute;
import com.example.i_commerce.domain.product.application.dto.CategoryAttributeGroupDto;
import com.example.i_commerce.domain.product.controller.response.CategoryAttributeResponse;
import com.example.i_commerce.domain.product.entity.Attribute;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.entity.CategoryAttribute;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.AttributeRepository;
import com.example.i_commerce.domain.product.repository.CategoryAttributeRepository;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.domain.product.repository.projection.CategoryAttributeKey;
import com.example.i_commerce.domain.product.repository.projection.CategoryAttributeProjection;
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
public class CategoryAttributeService {

    private final CategoryRepository categoryRepository;
    private final AttributeRepository attributeRepository;
    private final CategoryAttributeRepository categoryAttributeRepository;
    private final CategoryAttributeMapper categoryAttributeMapper;

    @Transactional(readOnly = true)
    public CategoryAttributeResponse getAttributesByCategory(Long categoryId) {

        if(!categoryRepository.existsById(categoryId)) {
            throw new AppException(ProductErrorCode.CATEGORY_NOT_FOUND);
        }

        List<CategoryAttributeProjection> projections =
            categoryAttributeRepository.findWithAttributeByCategoryId(categoryId);

        List<CategoryAttributeGroupDto> groupedCategoryAttributes =
            categoryAttributeMapper.toGroupResponse(projections);

        return CategoryAttributeResponse.of(categoryId, groupedCategoryAttributes);
    }

    @Transactional
    public AddCategoryAttributeResponse addCategoryAttribute(
        Long categoryId,
        AddCategoryAttributeRequest request
    ) {
        if(!categoryRepository.existsById(categoryId)) {
            throw new AppException(ProductErrorCode.CATEGORY_NOT_FOUND);
        }

        List<Attribute> attributes = attributeRepository.findAllById(request.attributeIds());
        if(request.attributeIds().size() != attributes.size()) {
            throw new AppException(ProductErrorCode.ATTRIBUTE_NOT_FOUND);
        }

        List<Long> targetCategoryIds = (request.propagateToChildren())
            ? categoryRepository.findAllDescendantIds(categoryId)
            : List.of(categoryId);

        Set<CategoryAttributeKey> existingKeys = new HashSet<>(
            categoryAttributeRepository.findExistingKeys(targetCategoryIds, request.attributeIds())
        );

        List<CategoryAttribute> categoryAttributes = new ArrayList<>();
        List<AlreadyExistsAttribute> existsAttributes = new ArrayList<>();

        for(Long targetCategoryId : targetCategoryIds) {
            Category categoryRef = categoryRepository.getReferenceById(targetCategoryId);
            for(Attribute attribute : attributes) {
                if(existingKeys.contains(new CategoryAttributeKey(targetCategoryId, attribute.getId()))) {
                    existsAttributes.add(AlreadyExistsAttribute.of(
                        targetCategoryId, attribute.getId()
                    ));
                    continue;
                }
                categoryAttributes.add(CategoryAttribute.of(
                    categoryRef,
                    attribute,
                    request.required()
                ));
            }
        }
        categoryAttributeRepository.saveAll(categoryAttributes);

        return AddCategoryAttributeResponse.of(categoryId, existsAttributes);
    }


    @Transactional
    public void deleteCategoryAttribute(Long categoryId, Long categoryAttributeId) {

        CategoryAttribute categoryAttribute = categoryAttributeRepository
            .findByIdAndCategoryId(categoryAttributeId, categoryId)
            .orElseThrow(() -> new AppException(ProductErrorCode.CATEGORY_ATTRIBUTE_NOT_FOUND));

        categoryAttributeRepository.delete(categoryAttribute);
    }

}
