package com.example.i_commerce.domain.product.repository;



import com.example.i_commerce.domain.product.entity.Product;
import com.example.i_commerce.domain.product.entity.QProduct;
import com.example.i_commerce.domain.product.entity.QProductItem;
import com.example.i_commerce.domain.product.entity.QProductOptionValue;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;



@Repository
@RequiredArgsConstructor
public class ProductQueryRepository {
    private final JPAQueryFactory queryFactory;

    private final QProductOptionValue productOptionValue1 = new QProductOptionValue("ov1");
    private final QProductOptionValue productOptionValue2 = new QProductOptionValue("ov2");

    public Optional<Product> findProductWithItems(Long productId) {
        Product result = queryFactory
            .selectFrom(QProduct.product)
            .distinct()
            .leftJoin(QProduct.product.items, QProductItem.productItem).fetchJoin()
            .leftJoin(QProductItem.productItem.optionValue1, productOptionValue1).fetchJoin()
            .leftJoin(QProductItem.productItem.optionValue2, productOptionValue2).fetchJoin()
            .leftJoin(QProduct.product.category).fetchJoin()
            .where(QProduct.product.id.eq(productId))
            .fetchOne();

        return Optional.ofNullable(result);
    }

}
