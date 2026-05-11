package com.example.i_commerce.domain.product.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QCategoryOption is a Querydsl query type for CategoryOption
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCategoryOption extends EntityPathBase<CategoryOption> {

    private static final long serialVersionUID = 195045665L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QCategoryOption categoryOption = new QCategoryOption("categoryOption");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    public final QCategory category;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final QOption option;

    public final BooleanPath required = createBoolean("required");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCategoryOption(String variable) {
        this(CategoryOption.class, forVariable(variable), INITS);
    }

    public QCategoryOption(Path<? extends CategoryOption> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QCategoryOption(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QCategoryOption(PathMetadata metadata, PathInits inits) {
        this(CategoryOption.class, metadata, inits);
    }

    public QCategoryOption(Class<? extends CategoryOption> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.category = inits.isInitialized("category") ? new QCategory(forProperty("category"), inits.get("category")) : null;
        this.option = inits.isInitialized("option") ? new QOption(forProperty("option")) : null;
    }

}

