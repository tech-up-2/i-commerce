package com.example.i_commerce.domain.product.application.service;


import com.example.i_commerce.domain.product.application.mapper.CategoryOptionMapper;
import com.example.i_commerce.domain.product.controller.response.CategoryOptionGroupResponse;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.CategoryOptionRepository;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.domain.product.repository.projection.CategoryOptionProjection;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryOptionService {

    private final CategoryRepository categoryRepository;
    private final CategoryOptionRepository categoryOptionRepository;

    private final CategoryOptionMapper categoryOptionMapper;

    @Transactional(readOnly = true)
    public List<CategoryOptionGroupResponse> getOptionsByCategory(Long categoryId) {

        if(!categoryRepository.existsById(categoryId)) {
            throw new AppException(ProductErrorCode.CATEGORY_NOT_FOUND);
        }

        List<CategoryOptionProjection> projections = categoryOptionRepository
            .findOptionsByCategoryId(categoryId);

        return categoryOptionMapper.toGroupedResponseList(projections);
    }

}
