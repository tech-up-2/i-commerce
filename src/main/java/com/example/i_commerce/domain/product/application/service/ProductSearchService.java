package com.example.i_commerce.domain.product.application.service;

import com.example.i_commerce.domain.product.application.dto.ProductSearchQuery;
import com.example.i_commerce.domain.product.controller.request.SearchProductRequest;
import com.example.i_commerce.domain.product.controller.response.ProductItemSearchResponse;
import com.example.i_commerce.domain.product.exception.ProductErrorCode;
import com.example.i_commerce.domain.product.repository.CategoryRepository;
import com.example.i_commerce.domain.product.repository.ProductSearchRepositoryCustom;
import com.example.i_commerce.domain.product.repository.enums.ProductSortType;
import com.example.i_commerce.global.exception.AppException;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;


@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ProductSearchService {

    private final ProductSearchRepositoryCustom productSearchRepository;
    private final CategoryRepository categoryRepository;

    private static final int GUEST_MAX_PAGE = 0;

    public Slice<ProductItemSearchResponse> search(
        SearchProductRequest request,
        Pageable pageable,
        boolean isAuthenticated
    ) {

        if (!isAuthenticated && pageable.getPageNumber() > GUEST_MAX_PAGE) {
            throw new AppException(ProductErrorCode.GUEST_PAGE_LIMIT_EXCEEDED);
        }

        String keyword = StringUtils.hasText(request.keyword())
            ? request.keyword().trim()
            : null;

        List<Long> categoryIds = (request.categoryId() == null)
            ? List.of()
            : categoryRepository.findAllDescendantIds(request.categoryId());

        ProductSortType sortType = resolveSortType(request.sortType(), keyword);

        ProductSearchQuery query = ProductSearchQuery.builder()
            .keyword(keyword)
            .categoryIds(categoryIds)
            .minPrice(request.minPrice())
            .maxPrice(request.maxPrice())
            .attributeIds(request.attributeIds())
            .sortType(sortType)
            .isAuthenticated(isAuthenticated)
            .build();

        return productSearchRepository.search(query, pageable);
    }

    private ProductSortType resolveSortType(
        ProductSortType requestedSort,
        String keyword
    ) {
        ProductSortType base = requestedSort != null
            ? requestedSort
            : ProductSortType.RELEVANCE;

        if(base == ProductSortType.RELEVANCE && keyword == null) {
            return ProductSortType.LATEST;
        }

        return base;
    }

}
