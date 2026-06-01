package com.example.i_commerce.domain.product.application.service;


import com.example.i_commerce.domain.product.application.mapper.CategoryMapper;
import com.example.i_commerce.domain.product.presentation.request.CreateCategoryRequest;
import com.example.i_commerce.domain.product.presentation.response.CreateCategoryResponse;
import com.example.i_commerce.domain.product.entity.Category;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.ProductRepository;
import com.example.i_commerce.domain.product.repository.projection.CategoryTreeRow;
import com.example.i_commerce.domain.product.presentation.response.CategoryResponse;
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
    private final ProductRepository productRepository;
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

        boolean isDuplicate = (parent == null)
            ? categoryRepository.existsByParentIsNullAndName(request.name())
            : categoryRepository.existsByParentAndName(parent, request.name());

        if (isDuplicate) {
            throw new AppException(ProductErrorCode.DUPLICATE_CATEGORY_NAME);
        }

        Category category = (parent == null)
            ? Category.createRoot(request.name())
            : Category.createChild(parent, request.name(), MAX_DEPTH);

        return CreateCategoryResponse.from(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long categoryId) {

        categoryRepository.findById(categoryId)
            .orElseThrow(() -> new AppException(ProductErrorCode.CATEGORY_NOT_FOUND));

        List<Long> categoryIds = categoryRepository.findAllDescendantIds(categoryId);

        if(productRepository.existsByCategoryIds(categoryIds)) {
            throw new AppException(ProductErrorCode.CATEGORY_HAS_PRODUCTS);
        }

        categoryRepository.deleteAllByIdInBatch(categoryIds);
    }


}
