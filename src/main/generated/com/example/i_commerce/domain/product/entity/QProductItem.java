package com.example.i_commerce.domain.product.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductItem is a Querydsl query type for ProductItem
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductItem extends EntityPathBase<ProductItem> {

    private static final long serialVersionUID = 265152852L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductItem productItem = new QProductItem("productItem");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    public final ListPath<ProductAttribute, QProductAttribute> attributes = this.<ProductAttribute, QProductAttribute>createList("attributes", ProductAttribute.class, QProductAttribute.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath displayOptionName = createString("displayOptionName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath isDefault = createBoolean("isDefault");

    public final StringPath mainImageUrl = createString("mainImageUrl");

    public final QProductOptionValue optionValue1;

    public final QProductOptionValue optionValue2;

    public final NumberPath<Integer> price = createNumber("price", Integer.class);

    public final QProduct product;

    public final StringPath sku = createString("sku");

    public final EnumPath<ProductItemStatus> status = createEnum("status", ProductItemStatus.class);

    public final QStock stock;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QProductItem(String variable) {
        this(ProductItem.class, forVariable(variable), INITS);
    }

    public QProductItem(Path<? extends ProductItem> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductItem(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductItem(PathMetadata metadata, PathInits inits) {
        this(ProductItem.class, metadata, inits);
    }

    public QProductItem(Class<? extends ProductItem> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.optionValue1 = inits.isInitialized("optionValue1") ? new QProductOptionValue(forProperty("optionValue1"), inits.get("optionValue1")) : null;
        this.optionValue2 = inits.isInitialized("optionValue2") ? new QProductOptionValue(forProperty("optionValue2"), inits.get("optionValue2")) : null;
        this.product = inits.isInitialized("product") ? new QProduct(forProperty("product"), inits.get("product")) : null;
        this.stock = inits.isInitialized("stock") ? new QStock(forProperty("stock"), inits.get("stock")) : null;
    }

}

