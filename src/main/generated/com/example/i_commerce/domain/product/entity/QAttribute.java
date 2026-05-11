package com.example.i_commerce.domain.product.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QAttribute is a Querydsl query type for Attribute
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAttribute extends EntityPathBase<Attribute> {

    private static final long serialVersionUID = -1473352754L;

    public static final QAttribute attribute = new QAttribute("attribute");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    public final ListPath<CategoryAttribute, QCategoryAttribute> categoryAttributes = this.<CategoryAttribute, QCategoryAttribute>createList("categoryAttributes", CategoryAttribute.class, QCategoryAttribute.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath key = createString("key");

    public final ListPath<ProductAttribute, QProductAttribute> productAttributes = this.<ProductAttribute, QProductAttribute>createList("productAttributes", ProductAttribute.class, QProductAttribute.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath value = createString("value");

    public QAttribute(String variable) {
        super(Attribute.class, forVariable(variable));
    }

    public QAttribute(Path<? extends Attribute> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAttribute(PathMetadata metadata) {
        super(Attribute.class, metadata);
    }

}

