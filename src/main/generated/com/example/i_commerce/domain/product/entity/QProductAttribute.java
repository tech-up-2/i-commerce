package com.example.i_commerce.domain.product.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QProductAttribute is a Querydsl query type for ProductAttribute
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QProductAttribute extends EntityPathBase<ProductAttribute> {

    private static final long serialVersionUID = 1990917371L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QProductAttribute productAttribute = new QProductAttribute("productAttribute");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    public final QAttribute attribute;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath displayName = createString("displayName");

    public final NumberPath<Integer> displayOrder = createNumber("displayOrder", Integer.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QProductItem productItem;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QProductAttribute(String variable) {
        this(ProductAttribute.class, forVariable(variable), INITS);
    }

    public QProductAttribute(Path<? extends ProductAttribute> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QProductAttribute(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QProductAttribute(PathMetadata metadata, PathInits inits) {
        this(ProductAttribute.class, metadata, inits);
    }

    public QProductAttribute(Class<? extends ProductAttribute> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.attribute = inits.isInitialized("attribute") ? new QAttribute(forProperty("attribute")) : null;
        this.productItem = inits.isInitialized("productItem") ? new QProductItem(forProperty("productItem"), inits.get("productItem")) : null;
    }

}

