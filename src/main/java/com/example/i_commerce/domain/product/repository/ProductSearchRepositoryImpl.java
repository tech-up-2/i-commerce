package com.example.i_commerce.domain.product.repository;

import com.example.i_commerce.domain.product.application.dto.ProductSearchQuery;
import com.example.i_commerce.domain.product.controller.response.ProductItemSearchResponse;
import com.example.i_commerce.domain.product.entity.ProductItemStatus;
import com.example.i_commerce.domain.product.entity.ProductStatus;
import com.example.i_commerce.domain.product.entity.QAttribute;
import com.example.i_commerce.domain.product.entity.QCategory;
import com.example.i_commerce.domain.product.entity.QProduct;
import com.example.i_commerce.domain.product.entity.QProductAttribute;
import com.example.i_commerce.domain.product.entity.QProductItem;
import com.example.i_commerce.domain.product.repository.enums.ProductSortType;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.NullExpression;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@Repository
@AllArgsConstructor
public class ProductSearchRepositoryImpl implements ProductSearchRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    private static final QProduct product = QProduct.product;
    private static final QProductItem productItem = QProductItem.productItem;
    private static final QCategory category = QCategory.category;
    private static final  QProductAttribute productAttribute = QProductAttribute.productAttribute;
    private static final QAttribute attribute = QAttribute.attribute;

    @Override
    public Slice<ProductItemSearchResponse> search(
        ProductSearchQuery query,
        Pageable pageable
    ) {
        Expression<Double> similarityScore = buildSimilarityExpression(
            query.keyword(),
            query.sortType()
        );

        List<ProductItemSearchResponse> results = queryFactory
            .select(Projections.constructor(ProductItemSearchResponse.class,
                productItem.id,
                product.id,
                product.name,
                productItem.displayOptionName,
                productItem.price,
                productItem.mainImageUrl,
                productItem.status,
                category.name,
                similarityScore
            ))
            .from(productItem)
            .join(productItem.product, product)
            .join(product.category, category)
            .where(
                productOnSaleOnly(),
                productItemOnSaleOnly(),
                keywordSearch(query.keyword()),
                categoryFilter(query.categoryIds()),
                priceFilter(query.minPrice(), query.maxPrice()),
                attributeFilter(query.attributeIds())
            )
            .orderBy(buildOrderSpecifier(
                query.sortType(),
                similarityScore
            ))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + 1L)
            .fetch();

        return toSlice(results, pageable);
    }

    private BooleanExpression productOnSaleOnly() {
        return product.status.eq(ProductStatus.ON_SALE);
    }

    private BooleanExpression productItemOnSaleOnly() {
        return productItem.status.eq(ProductItemStatus.ON_SALE);
    }

    private BooleanExpression keywordSearch(String keyword) {
        if (keyword == null || keyword.length() < 2) {
            return null;
        }

        BooleanExpression nameMatch = product.name.contains(keyword);

        BooleanExpression attributeMatch = JPAExpressions
            .selectOne()
            .from(productAttribute)
            .join(productAttribute.attribute, attribute)
            .where(
                productAttribute.productItem.id.eq(productItem.id),
                attribute.value.contains(keyword)
            )
            .exists();

        return nameMatch.or(attributeMatch);
    }

    private BooleanExpression categoryFilter(List<Long> categoryIds) {
        if (categoryIds.isEmpty()) {
            return null;
        }
        return product.category.id.in(categoryIds);
    }

    private BooleanExpression priceFilter(
        Integer minPrice,
        Integer maxPrice
    ) {
        if (minPrice == null && maxPrice == null) {
            return null;
        }
        if (minPrice == null) {
            return productItem.price.loe(maxPrice);
        }
        if (maxPrice == null) {
            return productItem.price.goe(minPrice);
        }
        return productItem.price.between(minPrice, maxPrice);
    }

    private BooleanExpression attributeFilter(
        List<Long> attributeIds
    ) {
        if (attributeIds.isEmpty()) {
            return null;
        }

        return attributeIds.stream()
            .map(this::existsAttribute)
            .reduce(BooleanExpression::and)
            .orElse(null);
    }

    private BooleanExpression existsAttribute(Long attributeId) {
        QProductAttribute pa = new QProductAttribute("pa_" + attributeId);
        return JPAExpressions
            .selectOne()
            .from(pa)
            .where(
                pa.productItem.id.eq(productItem.id),
                pa.attribute.id.eq(attributeId)
            )
            .exists();
    }

    private Expression<Double> buildSimilarityExpression(
        String keyword,
        ProductSortType sortType
    ) {
        if (sortType != ProductSortType.RELEVANCE || !StringUtils.hasText(keyword)) {
            return new NullExpression<>(Double.class);
        }

        return Expressions.numberTemplate(
            Double.class,
            "bigm_similarity({0}, {1})",
            product.name,
            keyword
        );
    }

    private OrderSpecifier<?>[] buildOrderSpecifier(
        ProductSortType sortType,
        Expression<Double> similarityScore
    ) {
        return switch (sortType) {
            case RELEVANCE -> new OrderSpecifier<?>[] {
                new OrderSpecifier<>(Order.DESC, similarityScore),
                productItem.id.asc()
            };
            case PRICE_ASC -> new OrderSpecifier<?>[] {
                productItem.price.asc(),
                productItem.id.asc()
            };
            case PRICE_DESC -> new OrderSpecifier<?>[] {
                productItem.price.desc(),
                productItem.id.asc()
            };
            case LATEST -> new OrderSpecifier<?>[] {
                productItem.createdAt.desc(),
                productItem.id.asc()
            };
        };
    }

    private Slice<ProductItemSearchResponse> toSlice(
        List<ProductItemSearchResponse> results,
        Pageable pageable
    ) {
        boolean hasNext = results.size() > pageable.getPageSize();

        if (hasNext) {
            results.removeLast();
        }

        return new SliceImpl<>(results, pageable, hasNext);
    }
}



