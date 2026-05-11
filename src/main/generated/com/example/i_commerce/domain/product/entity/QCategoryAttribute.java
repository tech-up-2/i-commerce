package com.example.i_commerce.domain.product.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCategoryAttribute is a Querydsl query type for CategoryAttribute
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCategoryAttribute extends EntityPathBase<CategoryAttribute> {

    private static final long serialVersionUID = 1977367152L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCategoryAttribute categoryAttribute = new QCategoryAttribute("categoryAttribute");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    public final QAttribute attribute;

    public final QCategory category;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final BooleanPath required = createBoolean("required");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCategoryAttribute(String variable) {
        this(CategoryAttribute.class, forVariable(variable), INITS);
    }

    public QCategoryAttribute(Path<? extends CategoryAttribute> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCategoryAttribute(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCategoryAttribute(PathMetadata metadata, PathInits inits) {
        this(CategoryAttribute.class, metadata, inits);
    }

    public QCategoryAttribute(Class<? extends CategoryAttribute> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.attribute = inits.isInitialized("attribute") ? new QAttribute(forProperty("attribute")) : null;
        this.category = inits.isInitialized("category") ? new QCategory(forProperty("category"), inits.get("category")) : null;
    }

}

