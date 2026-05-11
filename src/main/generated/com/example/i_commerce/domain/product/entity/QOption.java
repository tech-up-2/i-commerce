package com.example.i_commerce.domain.product.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QOption is a Querydsl query type for Option
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QOption extends EntityPathBase<Option> {

    private static final long serialVersionUID = -1696291709L;

    public static final QOption option = new QOption("option");

    public final com.example.i_commerce.global.common.entity.QBaseEntity _super = new com.example.i_commerce.global.common.entity.QBaseEntity(this);

    public final ListPath<CategoryOption, QCategoryOption> categoryOptions = this.<CategoryOption, QCategoryOption>createList("categoryOptions", CategoryOption.class, QCategoryOption.class, PathInits.DIRECT2);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    //inherited
    public final DateTimePath<java.time.LocalDateTime> deletedAt = _super.deletedAt;

    public final StringPath displayName = createString("displayName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath inputType = createString("inputType");

    public final StringPath type = createString("type");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public final StringPath value = createString("value");

    public QOption(String variable) {
        super(Option.class, forVariable(variable));
    }

    public QOption(Path<? extends Option> path) {
        super(path.getType(), path.getMetadata());
    }

    public QOption(PathMetadata metadata) {
        super(Option.class, metadata);
    }

}

