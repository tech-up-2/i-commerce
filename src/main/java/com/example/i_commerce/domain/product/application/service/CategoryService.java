package com.example.i_commerce.domain.product.application.service;


import com.example.i_commerce.domain.product.application.mapper.CategoryMapper;
import com.example.i_commerce.domain.product.controller.request.CreateCategoryRequest;
import com.example.i_commerce.domain.product.controller.response.CreateCategoryResponse;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.projection.CategoryTreeRow;
import com.example.i_commerce.domain.product.controller.response.CategoryResponse;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final int MAX_DEPTH = 5;
    private static final int DEFAULT_TREE_DEPTH = 3;
    private static final int RECURSIVE_DEPTH_LIMIT = 5;

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories(Integer requestDepth) {

        int maxDepth = (requestDepth == null)
            ? DEFAULT_TREE_DEPTH
            : Math.min(requestDepth, RECURSIVE_DEPTH_LIMIT);

        List<CategoryTreeRow> rows = categoryRepository.findAllCategoryTree(maxDepth);

        return categoryMapper.toHierarchy(rows);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        List<CategoryTreeRow> rows =
            categoryRepository.findCategoryTreeById(id, DEFAULT_TREE_DEPTH);

        if (rows.isEmpty()) {
            throw new AppException(ProductErrorCode.CATEGORY_NOT_FOUND);
        }

        return categoryMapper.toTree(rows);
    }

    @Transactional
    public CreateCategoryResponse createCategory(CreateCategoryRequest request) {

        Category parent = (request.parentId() == null)
            ? null
            : categoryRepository.findById(request.parentId())
              .orElseThrow(() -> new AppException(ProductErrorCode.CATEGORY_NOT_FOUND));

        Category category = (parent == null)
            ? Category.createRoot(request.name())
            : Category.createChild(parent, request.name(), MAX_DEPTH);

        return CreateCategoryResponse.from(categoryRepository.save(category));
    }


}
